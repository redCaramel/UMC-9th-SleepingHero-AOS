package com.umc_9th.sleepinghero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentClearBinding

class ClearFragment : Fragment() {

    private var _binding: FragmentClearBinding? = null
    private val binding get() = _binding!!

    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }
    private val sleepViewModel: SleepViewModel by viewModels { SleepViewModelFactory(sleepRepository) }

    private var selectedStar: Int = 0

    // 전달받는 값들
    private var recordId: Int = 0
    private var durationMinutes: Int = 0
    private var gainedExp: Int = 0
    private var currentLevel: Int = 0
    private var currentExp: Int = 0
    private var needExp: Int = 0

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

        readArgs()
        bindResultUi()
        setupStars()
        observeReviewResult()
        setupCompleteButton()
    }

    private fun readArgs() {
        arguments?.let { args ->
            recordId = args.getInt(KEY_RECORD_ID, 0)
            durationMinutes = args.getInt(KEY_DURATION_MINUTES, 0)
            gainedExp = args.getInt(KEY_GAINED_EXP, 0)
            currentLevel = args.getInt(KEY_CURRENT_LEVEL, 0)
            currentExp = args.getInt(KEY_CURRENT_EXP, 0)
            needExp = args.getInt(KEY_NEED_EXP, 0)
        }
    }

    private fun bindResultUi() {
        // 상단 "2시간 32분 수면" 같은 텍스트
        val (h, m) = minutesToHourMinute(durationMinutes)
        binding.tvSleepDuration.text = "${h}시간 ${m}분 수면"

        // 카드: 획득 경험치 / 현재 레벨
        binding.tvExpGained.text = "+${gainedExp} EXP"
        binding.tvLevelValue.text = if (currentLevel > 0) "Lv. $currentLevel" else "Lv. -"

        // TODO: 목표 수면 시간 동기화
        binding.tvTargetSleepValue.text = "07 H 30 M"

        // 실제 수면 시간
        binding.tvActualSleepValue.text = String.format("%02d H %02d M", h, m)

        // 다음 레벨까지: "248/300exp"
        binding.tvNextLevelValue.text =
            if (needExp > 0) "${currentExp}/${needExp}exp" else "-/-exp"
    }

    private fun setupStars() {
        // 초기값: 0점 (전부 빈별)
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
            if (token == null) {
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

            // 리뷰 작성 API 호출
            sleepViewModel.createSleepReview(
                token = token,
                recordId = recordId,
                star = selectedStar,
                comment = comment
            )
        }
    }

    private fun observeReviewResult() {
        sleepViewModel.sleepReviewResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                binding.btnComplete.isEnabled = true

                Toast.makeText(requireContext(), "저장 완료", Toast.LENGTH_SHORT).show()

                // TODO: AI 리뷰 결과 화면이 있으면 여기서 이동
                // Home으로 복귀
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container_main, HomeFragment())
                    .commit()
            }.onFailure { e ->
                binding.btnComplete.isEnabled = true
                Toast.makeText(requireContext(), "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun minutesToHourMinute(totalMinutes: Int): Pair<Int, Int> {
        val m = if (totalMinutes < 0) 0 else totalMinutes
        val h = m / 60
        val mm = m % 60
        return h to mm
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_RECORD_ID = "recordId"
        private const val KEY_DURATION_MINUTES = "durationMinutes"
        private const val KEY_GAINED_EXP = "gainedExp"
        private const val KEY_CURRENT_LEVEL = "currentLevel"
        private const val KEY_CURRENT_EXP = "currentExp"
        private const val KEY_NEED_EXP = "needExp"

        fun newInstance(
            recordId: Int,
            durationMinutes: Int,
            gainedExp: Int,
            currentLevel: Int,
            currentExp: Int,
            needExp: Int
        ): ClearFragment {
            return ClearFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_RECORD_ID, recordId)
                    putInt(KEY_DURATION_MINUTES, durationMinutes)
                    putInt(KEY_GAINED_EXP, gainedExp)
                    putInt(KEY_CURRENT_LEVEL, currentLevel)
                    putInt(KEY_CURRENT_EXP, currentExp)
                    putInt(KEY_NEED_EXP, needExp)
                }
            }
        }
    }
}