package com.umc_9th.sleepinghero.ui.hero

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.umc_9th.sleepinghero.BuildConfig
import com.umc_9th.sleepinghero.HeroCondition
import com.umc_9th.sleepinghero.HeroSkinUtil
import com.umc_9th.sleepinghero.R
import com.umc_9th.sleepinghero.SleepRecordAdapter
import com.umc_9th.sleepinghero.SleepRecordUiModel
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.HeroRepository
import com.umc_9th.sleepinghero.api.repository.HomeRepository
import com.umc_9th.sleepinghero.api.repository.SkinRepository
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.databinding.FragmentHeroBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class HeroFragment : Fragment() {

    private var _binding: FragmentHeroBinding? = null
    private val binding get() = _binding!!

    private var currentCondition = HeroCondition.BEST
    private var baseSkinId: Long = 1L

    private val heroRepository by lazy { HeroRepository() }
    private val homeRepository by lazy { HomeRepository(ApiClient.homeService) }
    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }
    private val sleepAdapter by lazy { SleepRecordAdapter() }
    private val skinRepository by lazy { SkinRepository() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvRecentSleep.adapter = sleepAdapter
        fetchRecentSleep()
        fetchHeroInfo()
        fetchDashboard()
        fetchEquippedSkinAndApply()
    }

    private fun fetchHeroInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch

            val token = "Bearer $raw"
            val res = heroRepository.getMyHero(token)

            if (res.isSuccess && res.result != null) {
                val hero = res.result

                val level = hero.currentLevel
                val currentExpInLevel = hero.currentExp
                val needExp = hero.needExp
                // 만약 서버가 needExp를 안주거나 0이면 공식으로 대체:
                // .takeIf { it > 0 } ?: needExpForLevel(level)

                val baseAbs = baseAbsExpAtLevelStart(level)
                val currentAbs = baseAbs
                val nextAbs = baseAbs + needExp

                binding.tvLevelLeft.text = "Level $level"
                binding.tvLevelRight.text = "Level ${level + 1}"

                binding.tvExpLeft.text = "${currentAbs} EXP"
                binding.tvExpRight.text = "${nextAbs} EXP"

                val progress = if (needExp > 0) {
                    (currentExpInLevel * 100 / needExp).coerceIn(0, 100)
                } else 0
                binding.progressExp.progress = progress
            }
        }
    }

    private fun fetchDashboard() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch

            val result = homeRepository.getDashboard(raw)

            result.onSuccess { data ->
                val nonSleepStreak = data.nonSleepStreak

                currentCondition = HeroSkinUtil.calculateCondition(nonSleepStreak)

                applyResolvedSkinIfReady()

                val (statusText, messageText, penaltyPercent) = when {
                    nonSleepStreak <= 0 ->
                        Triple("최상 컨디션", "연속적으로 숙면 중이에요!", 0)

                    nonSleepStreak == 1 ->
                        Triple("컨디션 저하", "어제의 기록이 비었어요.\n오늘 한 번에 회복해요!", 5)

                    nonSleepStreak == 2 ->
                        Triple("무기력함", "획득 경험치가 살짝 줄어들 수 있어요!", 10)

                    else ->
                        Triple("감기", "감기 기운이 도네요.\n짧은 회복 퀘스트로 금방 나아요!", 20)
                }

                binding.tvCondition.text = statusText
                binding.tvHeroMessage.text = messageText

                updateProgressBarColor(currentCondition)

                if (penaltyPercent == 0) {
                    binding.layoutConditionDetail.visibility = View.GONE
                } else {
                    binding.layoutConditionDetail.visibility = View.VISIBLE
                    binding.tvNonSleepStreak.text = "연속 무기록 : ${nonSleepStreak}일"
                    setPenaltyTextWithRedPercent(penaltyPercent)
                }
            }.onFailure { e ->
                Log.e("DASHBOARD_ERROR", e.message ?: "unknown error")
            }
        }
    }

    private fun fetchRecentSleep() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch

            val result = sleepRepository.getSleepSessions(raw, page = 0, size = 10)

            result.onSuccess { data ->

                val uiList = data.content
                    .filter { it.totalMinutes > 0 }   // ✅ 0시간 0분이면 저장 안 함
                    .map { dto ->

                        val durationText = minutesToHourMin(dto.totalMinutes)

                        val dateText = dto.sleptTime.take(10)

                        SleepRecordUiModel(
                            date = dateText,
                            sleepTimeText = durationText,
                            star = dto.star.coerceIn(0, 5),
                            advice = dto.summary ?: ""
                        )
                    }

                sleepAdapter.submitList(uiList)
            }.onFailure { e ->
                Log.e("SLEEP_ERROR", e.message ?: "unknown error")
            }
        }
    }

    private fun minutesToHourMin(totalMinutes: Int): String {
        val safe = totalMinutes.coerceAtLeast(0)
        val h = safe / 60
        val m = safe % 60
        return "${h}시간 ${m}분"
    }


    private fun fetchEquippedSkinAndApply() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch

            val token = "Bearer $raw"
            val res = skinRepository.getMySkins(token)

            if (!res.isSuccess || res.result == null) {
                Log.e("SKIN_ERR", res.message)
                return@launch
            }
            baseSkinId = res.result.skins.firstOrNull { it.equipped }?.skinId ?: 1L

            val equippedSkinId = res.result.skins.firstOrNull { it.equipped }?.skinId ?: 1L
            applySkinImage(equippedSkinId)
        }
    }

    private fun applyResolvedSkinIfReady() {
        val resolvedSkinId = HeroSkinUtil.resolveSkin(baseSkinId, currentCondition)
        applySkinImage(resolvedSkinId)
    }

    private fun applySkinImage(skinId: Long) {
        val resId = resources.getIdentifier("hero_skin_${skinId}", "drawable", requireContext().packageName)
        binding.ivHero.setImageResource(if (resId != 0) resId else R.drawable.hero_skin_1)
    }

    private fun setPenaltyTextWithRedPercent(penaltyPercent: Int) {

        val percentText = "-${penaltyPercent} %"
        val full = "효과 : $percentText exp"

        val spannable = SpannableString(full)

        val start = full.indexOf(percentText)
        if (start >= 0) {
            val end = start + percentText.length
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#B91C14")),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvExpPenalty.text = spannable
    }

    private fun updateProgressBarColor(condition: HeroCondition) {
        val color = when (condition) {
            HeroCondition.BEST -> Color.parseColor("#2FA4A9")
            HeroCondition.LOW  -> Color.parseColor("#FAAC10")
            HeroCondition.BAD -> Color.parseColor("#F23120")
            HeroCondition.COLD -> Color.parseColor("#8A120A")
        }

        binding.progressExp.progressTintList = ColorStateList.valueOf(color)
    }

    private fun needExpForLevel(level: Int): Int {
        // level L -> L+1 필요 exp
        return 100 + (level - 1) * 10
    }

    private fun baseAbsExpAtLevelStart(level: Int): Int {
        // level 1 시작은 0
        if (level <= 1) return 0

        // sum_{k=1..level-1} (100 + (k-1)*10)
        // = (level-1)*100 + 10 * sum_{k=1..level-1} (k-1)
        // = (level-1)*100 + 10 * ((level-2)(level-1)/2)
        val n = level - 1
        return n * 100 + 10 * ((n - 1) * n / 2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
