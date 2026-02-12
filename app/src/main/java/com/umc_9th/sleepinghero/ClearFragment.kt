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
import com.umc_9th.sleepinghero.ui.hero.HeroFragment

class ClearFragment : Fragment() {

    private var _binding: FragmentClearBinding? = null
    private val binding get() = _binding!!

    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }
    private val sleepViewModel: SleepViewModel by viewModels { SleepViewModelFactory(sleepRepository) }

    private var selectedStar: Int = 0

    // endSleep로 채워질 값들
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

        setupStars()
        observeEndSleepResult()
        observeReviewResult()
        setupCompleteButton()

        requestEndSleepAndBind()
    }

    private fun requestEndSleepAndBind() {
        val token = TokenManager.getAccessToken(requireContext())
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 로딩 중 중복 처리 방지
        binding.btnComplete.isEnabled = false

        // 수면 종료 호출 → 보상/경험치/레벨 정보 받아서 UI 세팅
        sleepViewModel.endSleep(token)
    }

    private fun observeEndSleepResult() {
        sleepViewModel.sleepEndResult.observe(viewLifecycleOwner) { result ->
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

        // 목표 수면 시간(일단 고정)
        binding.tvTargetSleepValue.text = "07 H 30 M"

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
            result.onSuccess {
                binding.btnComplete.isEnabled = true
                Toast.makeText(requireContext(), "저장 완료", Toast.LENGTH_SHORT).show()

                // HeroFragment로 이동
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container_main, HeroFragment())
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
}
