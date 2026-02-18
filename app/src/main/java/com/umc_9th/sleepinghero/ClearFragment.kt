package com.umc_9th.sleepinghero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import com.umc_9th.sleepinghero.databinding.FragmentClearBinding
import com.umc_9th.sleepinghero.ui.hero.HeroFragment
import kotlinx.coroutines.launch

class ClearFragment : Fragment() {

    private var _binding: FragmentClearBinding? = null
    private val binding get() = _binding!!

    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }
    private val socialRepository by lazy { SocialRepository(ApiClient.socialService) }

    private var selectedStar: Int = 0


    // endSleep로 채워질 값들 (arguments에서 초기화 가능)
    private var recordId: Int = 0
    private var durationMinutes: Int = 0
    private var gainedExp: Int = 0
    private var currentLevel: Int = 0
    private var currentExp: Int = 0
    private var needExp: Int = 0
    private var sleepTimeStr: String = "11:00 PM"
    private var awakeTimeStr: String = "07:00 AM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            recordId = args.getInt(ARG_RECORD_ID, 0)
            durationMinutes = args.getInt(ARG_DURATION_MINUTES, 0)
            gainedExp = args.getInt(ARG_GAINED_EXP, 0)
            currentLevel = args.getInt(ARG_CURRENT_LEVEL, 0)
            currentExp = args.getInt(ARG_CURRENT_EXP, 0)
            needExp = args.getInt(ARG_NEED_EXP, 0)
            sleepTimeStr = args.getString(ARG_SLEEP_TIME_STR) ?: "11:00 PM"
            awakeTimeStr = args.getString(ARG_AWAKE_TIME_STR) ?: "07:00 AM"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStars()
        setupCompleteButton()

        loadUserInfoAndUpdateTitle()
        // arguments로 값이 넘어온 경우(예: SleepTracker에서 수면 종료) 먼저 UI 반영
        if (arguments?.containsKey(ARG_DURATION_MINUTES) == true) {
            bindResultUi()
        }
        requestEndSleepAndBind()
    }

    private fun requestEndSleepAndBind() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 로딩 중 중복 처리 방지
            binding.btnComplete.isEnabled = false

            // 수면 종료 호출 → 보상/경험치/레벨 정보 받아서 UI 세팅
            val result = sleepRepository.endSleep(token)

            result.onSuccess { data ->
                // end 응답으로 값 세팅
                recordId = data.recordId
                durationMinutes = data.durationMinutes
                gainedExp = data.sleepReward.gainedExp

                // 질병/디버프는 일단 무시: data.sleepReward.isDebuff

                val lc = data.sleepReward.levelChange
                if (lc != null) {
                    currentLevel = lc.currentLevel
                    currentExp = lc.currentExp
                    needExp = lc.needExp
                } else {
                    // 레벨변동 정보가 없을 수 있으니 안전 처리
                    currentLevel = 0
                    currentExp = 0
                    needExp = 0
                }

                bindResultUi()

                // end 성공하면 이제 리뷰 저장 가능
                binding.btnComplete.isEnabled = true
            }.onFailure { e ->
                binding.btnComplete.isEnabled = true
                Log.e("CLEAR_FRAGMENT", "수면 종료 실패: ${e.message}")
                Toast.makeText(requireContext(), "수면 종료 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindResultUi() {
        val (h, m) = minutesToHourMinute(durationMinutes)

        // 상단 "2시간 32분 수면"
        binding.tvSleepDuration.text = "${h}시간 ${m}분 수면"

        // 획득 경험치 / 현재 레벨
        binding.tvExpGained.text = "+${gainedExp} EXP"
        binding.tvLevelValue.text = if (currentLevel > 0) "Lv. $currentLevel" else "Lv. -"

        // 목표 수면 시간: 설정한 취침~기상 시간 (예: 08 H 00 M)
        binding.tvTargetSleepValue.text = formatGoalTimeForDisplay(sleepTimeStr, awakeTimeStr)

        // 실제 수면 시간
        binding.tvActualSleepValue.text = String.format("%02d H %02d M", h, m)

        // 다음 레벨까지 exp
        binding.tvNextLevelValue.text =
            if (needExp > 0) "${currentExp}/${needExp}exp" else "-/-exp"
    }

    private fun setupStars() {
        setStarRating(0)

        binding.star1.setOnClickListener { setStarRating(1) }
        binding.star2.setOnClickListener { setStarRating(2) }
        binding.star3.setOnClickListener { setStarRating(3) }
        binding.star4.setOnClickListener { setStarRating(4) }
        binding.star5.setOnClickListener { setStarRating(5) }
    }

    private fun setStarRating(rating: Int) {
        selectedStar = rating.coerceIn(0, 5)

        val filled = R.drawable.home_clear_staricon
        val empty = R.drawable.home_clear_staremptyicon

        binding.star1.setImageResource(if (selectedStar >= 1) filled else empty)
        binding.star2.setImageResource(if (selectedStar >= 2) filled else empty)
        binding.star3.setImageResource(if (selectedStar >= 3) filled else empty)
        binding.star4.setImageResource(if (selectedStar >= 4) filled else empty)
        binding.star5.setImageResource(if (selectedStar >= 5) filled else empty)
    }

    private fun setupCompleteButton() {
        binding.btnComplete.setOnClickListener {
            val token = TokenManager.getAccessToken(requireContext())
            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (recordId == 0) {
                Toast.makeText(requireContext(), "recordId가 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedStar == 0) {
                Toast.makeText(requireContext(), "별점을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comment = binding.etMemo.text?.toString()?.trim().orEmpty()

            // 중복 클릭 방지
            binding.btnComplete.isEnabled = false

            // 리뷰 작성 API 호출 (HeroFragment와 동일한 패턴)
            viewLifecycleOwner.lifecycleScope.launch {
                val result = sleepRepository.createSleepReview(
                    token = token,
                    recordId = recordId,
                    star = selectedStar,
                    comment = comment
                )

                result.onSuccess {
                    binding.btnComplete.isEnabled = true
                    Toast.makeText(requireContext(), "저장 완료", Toast.LENGTH_SHORT).show()

                    // HeroFragment로 이동
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container_main, HeroFragment())
                        .commit()
                }.onFailure { e ->
                    binding.btnComplete.isEnabled = true
                    Log.e("CLEAR_FRAGMENT", "리뷰 작성 실패: ${e.message}")
                    Toast.makeText(requireContext(), "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun minutesToHourMinute(totalMinutes: Int): Pair<Int, Int> {
        val m = if (totalMinutes < 0) 0 else totalMinutes
        val h = m / 60
        val mm = m % 60
        return h to mm
    }

    /** 취침/기상 문자열으로 목표 수면 시간 "HH H MM M" 형식 반환 */
    private fun formatGoalTimeForDisplay(sleepStr: String, awakeStr: String): String {
        val (sh, sm, spm) = parseTimeForGoal(sleepStr)
        val (ah, am, apm) = parseTimeForGoal(awakeStr)
        fun toMinutes(h12: Int, m: Int, pm: Int): Int {
            var h24 = h12 % 12
            if (pm == 1) h24 += 12
            return h24 * 60 + m
        }
        val sleepMin = toMinutes(sh, sm, spm)
        val awakeMin = toMinutes(ah, am, apm)
        val totalMin = if (awakeMin > sleepMin) awakeMin - sleepMin else (24 * 60 - sleepMin) + awakeMin
        val h = totalMin / 60
        val mm = totalMin % 60
        return String.format("%02d H %02d M", h, mm)
    }

    private fun parseTimeForGoal(timeStr: String): Triple<Int, Int, Int> {
        return try {
            val parts = timeStr.trim().split(" ")
            val time = parts.getOrNull(0) ?: "11:00"
            val ampmStr = parts.getOrNull(1) ?: "PM"
            val hm = time.split(":")
            val hour = hm.getOrNull(0)?.toIntOrNull() ?: 11
            val minute = hm.getOrNull(1)?.toIntOrNull() ?: 0
            val ampmFlag = if (ampmStr.equals("PM", ignoreCase = true)) 1 else 0
            Triple(hour, minute, ampmFlag)
        } catch (e: Exception) {
            Triple(11, 0, 1)
        }
    }

    private fun loadUserInfoAndUpdateTitle() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            if (token.isNullOrEmpty()) return@launch
            val result = socialRepository.MyCharacterCheck(token)
            result.onSuccess { data ->
                binding.tvClearTitle.text = "${data.name}님, 수면 기록 완료!"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_RECORD_ID = "arg_record_id"
        private const val ARG_DURATION_MINUTES = "arg_duration_minutes"
        private const val ARG_GAINED_EXP = "arg_gained_exp"
        private const val ARG_CURRENT_LEVEL = "arg_current_level"
        private const val ARG_CURRENT_EXP = "arg_current_exp"
        private const val ARG_NEED_EXP = "arg_need_exp"

        private const val ARG_SLEEP_TIME_STR = "arg_sleep_time_str"
        private const val ARG_AWAKE_TIME_STR = "arg_awake_time_str"

        fun newInstance(
            recordId: Int,
            durationMinutes: Int,
            gainedExp: Int,
            currentLevel: Int,
            currentExp: Int,
            needExp: Int,
            sleepTimeStr: String = "11:00 PM",
            awakeTimeStr: String = "07:00 AM"
        ): ClearFragment {
            return ClearFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RECORD_ID, recordId)
                    putInt(ARG_DURATION_MINUTES, durationMinutes)
                    putInt(ARG_GAINED_EXP, gainedExp)
                    putInt(ARG_CURRENT_LEVEL, currentLevel)
                    putInt(ARG_CURRENT_EXP, currentExp)
                    putInt(ARG_NEED_EXP, needExp)
                    putString(ARG_SLEEP_TIME_STR, sleepTimeStr)
                    putString(ARG_AWAKE_TIME_STR, awakeTimeStr)
                }
            }
        }
    }
}