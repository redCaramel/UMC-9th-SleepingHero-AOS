package com.umc_9th.sleepinghero.ui.hero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.umc_9th.sleepinghero.SleepRecordAdapter
import com.umc_9th.sleepinghero.SleepRecordUiModel
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.HeroRepository
import com.umc_9th.sleepinghero.api.repository.HomeRepository
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.databinding.FragmentHeroBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class HeroFragment : Fragment() {

    private var _binding: FragmentHeroBinding? = null
    private val binding get() = _binding!!

    private val heroRepository by lazy {
        HeroRepository()
    }

    private val homeRepository by lazy {
        HomeRepository()
    }

    private val sleepRepository by lazy { SleepRepository() }
    private val sleepAdapter by lazy { SleepRecordAdapter() }

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
    }

    private fun fetchHeroInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch

            val token = "Bearer $raw"
            val res = heroRepository.getMyHero(token)

            if (res.isSuccess && res.result != null) {
                val hero = res.result

                binding.tvLevelLeft.text = "Level ${hero.currentLevel}"
                binding.tvExpLeft.text = "${hero.currentExp} EXP"
                binding.tvLevelRight.text = "Level ${hero.currentLevel + 1}"
                binding.tvExpRight.text = "${hero.needExp} EXP"

                val progress =
                    (hero.currentExp * 100 / hero.needExp).coerceIn(0, 100)
                binding.progressExp.progress = progress
            }
        }
    }

    private fun fetchDashboard() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch

            val token = "Bearer $raw"
            val res = homeRepository.getDashboard(token)

            if (res.isSuccess && res.result != null) {
                val nonSleepStreak = res.result.nonSleepStreak

                val (statusText, messageText, penaltyPercent) = when {
                    nonSleepStreak <= 0 ->
                        Triple("최상 컨디션", "연속적으로 숙면 중이에요!", 0)

                    nonSleepStreak == 1 ->
                        Triple("컨디션 저하", "컨디션이 살짝 떨어졌어요.", 5)

                    nonSleepStreak == 2 ->
                        Triple("무기력함", "무기력이 쌓이고 있어요.", 10)

                    else ->
                        Triple("감기", "컨디션이 많이 나빠요.", 20)
                }

                binding.tvCondition.text = statusText
                binding.tvHeroMessage.text = messageText

                if (penaltyPercent == 0) {
                    binding.layoutConditionDetail.visibility = View.GONE
                } else {
                    binding.layoutConditionDetail.visibility = View.VISIBLE
                    binding.tvNonSleepStreak.text = "연속 무기록 : ${nonSleepStreak}일"
                    binding.tvExpPenalty.text = "효과 : -${penaltyPercent} % exp"
                }
            } else {
                Log.e("DASHBOARD_ERROR", res.message)
            }
        }
    }


    private fun fetchRecentSleep() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch
            val token = "Bearer $raw"

            val res = sleepRepository.getSleepSessions(token, page = 0, size = 10)

            if (!res.isSuccess || res.result == null) {
                // TODO: 실패 처리(토스트/로그)
                return@launch
            }

            val uiList = res.result.content.map { dto ->
                val durationText = calcSleepDurationText(dto.sleptTime, dto.wokeTime)
                val dateText = dto.sleptTime.take(10) // "YYYY-MM-DD" (일단 sleptTime 기준)

                SleepRecordUiModel(
                    date = dateText,
                    sleepTimeText = durationText,
                    star = if (dto.isSuccess) 5 else 1, // ★ 임시 매핑 (리뷰 API 붙이면 교체)
                    advice = if (dto.isSuccess) "용사의 조언: 잘 잤군! 오늘도 힘내자!" else "용사의 조언: 용사여… 휴식이 부족하군!"
                )
            }

            sleepAdapter.submitList(uiList)
        }
    }

    private fun calcSleepDurationText(sleptTime: String, wokeTime: String): String {
        return try {
            val start = java.time.LocalDateTime.parse(sleptTime)
            val end = java.time.LocalDateTime.parse(wokeTime)
            val minutes = java.time.Duration.between(start, end).toMinutes().toInt().coerceAtLeast(0)

            val h = minutes / 60
            val m = minutes % 60
            "${h}시간 ${m}분"
        } catch (e: Exception) {
            "알 수 없음"
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
