package com.umc_9th.sleepinghero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.dto.CharacterInfoResponse
import com.umc_9th.sleepinghero.api.dto.HomeDashboardResponse
import com.umc_9th.sleepinghero.api.repository.CharacterRepository
import com.umc_9th.sleepinghero.api.repository.FriendRepository
import com.umc_9th.sleepinghero.api.repository.HomeRepository
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.api.viewmodel.CharacterViewModel
import com.umc_9th.sleepinghero.api.viewmodel.CharacterViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.FriendViewModel
import com.umc_9th.sleepinghero.api.viewmodel.FriendViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.HomeViewModel
import com.umc_9th.sleepinghero.api.viewmodel.HomeViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Home Repository & ViewModel
    private val homeRepository by lazy {
        HomeRepository(ApiClient.homeService)
    }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(homeRepository)
    }

    // Character Repository & ViewModel
    private val characterRepository by lazy {
        CharacterRepository(ApiClient.characterService)
    }

    private val characterViewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory(characterRepository)
    }

    // Friend Repository & ViewModel
    private val friendRepository by lazy {
        FriendRepository(ApiClient.friendService)
    }

    private val friendViewModel: FriendViewModel by viewModels {
        FriendViewModelFactory(friendRepository)
    }

    // Sleep Repository & ViewModel
    private val sleepRepository by lazy {
        SleepRepository(ApiClient.sleepService)
    }

    private val sleepViewModel: SleepViewModel by viewModels {
        SleepViewModelFactory(sleepRepository)
    }

    // 데이터 캐시
    private var characterInfo: CharacterInfoResponse? = null
    private var dashboardData: HomeDashboardResponse? = null
    private var myRanking: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupButtons()
        observeData()
        loadAllData()

        return binding.root
    }

    private fun setupButtons() {
        // 컨디션 버튼
        binding.btnDiary.setOnClickListener {
            Toast.makeText(requireContext(), "컨디션 화면 준비중", Toast.LENGTH_SHORT).show()
        }

        // 수면 시작하기 버튼
        binding.btnStartSleep.setOnClickListener {
            val auth = TokenManager.getAuthHeader(requireContext())
            if (auth != null) {
                sleepViewModel.startSleep(auth)
            } else {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }


        // 맵 확대 버튼
        binding.btnZoomIn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, MapFragment())
                .addToBackStack(null)
                .commit()
        }

        // 취침 시간 설정 (현재는 조회만)
        binding.bedtimeContainer.setOnClickListener {
            Toast.makeText(requireContext(), "최근 수면 기록에서 가져온 시간입니다", Toast.LENGTH_SHORT).show()
        }

        // 기상 시간 설정 (현재는 조회만)
        binding.wakeupContainer.setOnClickListener {
            Toast.makeText(requireContext(), "최근 수면 기록에서 가져온 시간입니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeData() {
        // Character 정보 관찰
        characterViewModel.characterInfo.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                characterInfo = data
                updateCharacterUI()
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "캐릭터 정보 로드 실패: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Dashboard 정보 관찰
        homeViewModel.homeDashboard.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                dashboardData = data
                updateDashboardUI()
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "대시보드 로드 실패: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Friend 랭킹 관찰
        friendViewModel.friendRanking.observe(viewLifecycleOwner) { result ->
            result.onSuccess { rankings ->
                val myNickname = characterInfo?.name
                myRanking = rankings.find { it.nickname == myNickname }?.rank
                updateRankingUI()
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "친구 랭킹 로드 실패: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 수면 시작 결과 관찰
        sleepViewModel.sleepStartResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "수면을 시작했습니다",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.container_main, SleepTrackerFragment())
                    .addToBackStack(null)
                    .commit()
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "수면 시작 실패: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 수면 기록 목록 관찰 (총 수면 시간 + 최근 취침/기상 시간)
        sleepViewModel.sleepSessions.observe(viewLifecycleOwner) { result ->
            result.onSuccess { sessions ->
                if (sessions.content.isNotEmpty()) {
                    // 가장 최근 수면 기록
                    val latestSession = sessions.content.first()

                    // ✅ 취침 시간 (sleptTime)
                    binding.tvBedtimeValue.text = formatTimeToAMPM(latestSession.sleptTime)

                    // ✅ 기상 시간 (wokeTime)
                    binding.tvWakeupValue.text = formatTimeToAMPM(latestSession.wokeTime)

                    // ✅ 목표 수면 시간 (실제 수면 시간으로 계산)
                    val durationHours = calculateDurationHours(
                        latestSession.sleptTime,
                        latestSession.wokeTime
                    )
                    binding.tvSleepGoalValue.text = "${durationHours}시간"

                    // 총 수면 시간 (전체 기록 합산)
                    val totalHours = calculateTotalSleepHours(sessions.content)
                    binding.tvClockValue.text = "${totalHours}시간"
                } else {
                    // 기록이 없을 때 더미 데이터
                    binding.tvBedtimeValue.text = "11:00 PM"
                    binding.tvWakeupValue.text = "07:00 AM"
                    binding.tvSleepGoalValue.text = "8시간"
                    binding.tvClockValue.text = "0시간"
                }
            }.onFailure {
                // 실패 시 더미 데이터
                binding.tvBedtimeValue.text = "11:00 PM"
                binding.tvWakeupValue.text = "07:00 AM"
                binding.tvSleepGoalValue.text = "8시간"
                binding.tvClockValue.text = "70시간"
            }
        }
    }

    private fun loadAllData() {
        val auth = TokenManager.getAuthHeader(requireContext())

        if (auth != null) {
            characterViewModel.loadCharacterInfo(auth)
            homeViewModel.loadHomeDashboard(auth)
            friendViewModel.loadFriendRanking(auth)
            sleepViewModel.loadSleepSessions(auth, page = 0, size = 30)
        } else {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateCharacterUI() {
        val character = characterInfo ?: return

        // 레벨 및 이름
        binding.tvUserLevelName.text = "LV.${character.currentLevel} ${character.name} 님"

        // 다음 레벨까지 EXP
        val expRemaining = character.needExp - character.currentExp
        binding.tvExpRemaining.text = "-$expRemaining EXP"

        // EXP 프로그레스 바
        val expPercentage = if (character.needExp > 0) {
            ((character.currentExp.toFloat() / character.needExp) * 100).toInt()
        } else {
            0
        }
        updateExpBar(expPercentage)
    }

    private fun updateDashboardUI() {
        val data = dashboardData ?: return

        // 연속 수면일
        binding.tvCalendarValue.text = "${data.currentStreak}일"
    }

    private fun updateRankingUI() {
        val rank = myRanking ?: return
        binding.tvTrophyValue.text = "${rank}등"
    }

    private fun updateExpBar(percentage: Int) {
        binding.progressExp.post {
            val containerWidth = binding.progressExpBg.width
            if (containerWidth > 0) {
                val progress = percentage / 100f
                val progressWidth = (containerWidth * progress).toInt()

                val layoutParams = binding.progressExp.layoutParams
                layoutParams.width = progressWidth
                binding.progressExp.layoutParams = layoutParams
            }
        }

        binding.tvExpPercentage.text = "$percentage%"
    }

    /**
     * ISO 8601 시간 → AM/PM 형식 변환
     * "2026-02-11T23:00:00.000Z" → "11:00 PM"
     */
    //TODO: 목표 수면 시간 설정 동기화
    private fun formatTimeToAMPM(isoTime: String): String {
        return try {
            // ISO 8601 파싱
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoTime)

            // AM/PM 형식으로 변환 (로컬 시간)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            outputFormat.timeZone = TimeZone.getDefault()
            outputFormat.format(date ?: Date()).uppercase(Locale.ENGLISH)
        } catch (e: Exception) {
            "00:00 AM"
        }
    }

    /**
     * 두 시간 사이의 시간 차이 계산 (시간 단위)
     */
    private fun calculateDurationHours(sleptTime: String, wokeTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val sleptDate = inputFormat.parse(sleptTime)
            val wokeDate = inputFormat.parse(wokeTime)

            if (sleptDate != null && wokeDate != null) {
                val diffMillis = wokeDate.time - sleptDate.time
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60

                if (minutes > 0) {
                    "${hours}시간 ${minutes}분"
                } else {
                    "${hours}시간"
                }
            } else {
                "8시간"
            }
        } catch (e: Exception) {
            "8시간"
        }
    }

    /**
     * 수면 기록 목록에서 총 수면 시간 계산
     */
    private fun calculateTotalSleepHours(sessions: List<com.umc_9th.sleepinghero.api.dto.SleepSessionItem>): Int {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            var totalMillis = 0L

            for (session in sessions) {
                val sleptDate = inputFormat.parse(session.sleptTime)
                val wokeDate = inputFormat.parse(session.wokeTime)

                if (sleptDate != null && wokeDate != null) {
                    totalMillis += (wokeDate.time - sleptDate.time)
                }
            }

            TimeUnit.MILLISECONDS.toHours(totalMillis).toInt()
        } catch (e: Exception) {
            70 // 실패 시 더미 값
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}