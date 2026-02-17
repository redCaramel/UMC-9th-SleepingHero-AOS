package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.dto.CharacterInfoResponse
import com.umc_9th.sleepinghero.api.dto.SleepSessionItem
import com.umc_9th.sleepinghero.api.dto.DashBoardResponse
import com.umc_9th.sleepinghero.api.repository.CharacterRepository
import com.umc_9th.sleepinghero.api.repository.FriendRepository
import com.umc_9th.sleepinghero.api.repository.HomeRepository
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.api.repository.SocialRepository
import kotlinx.coroutines.launch
import com.umc_9th.sleepinghero.api.viewmodel.CharacterViewModel
import com.umc_9th.sleepinghero.api.viewmodel.CharacterViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.FriendViewModel
import com.umc_9th.sleepinghero.api.viewmodel.FriendViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.HomeViewModel
import com.umc_9th.sleepinghero.api.viewmodel.HomeViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModelFactory
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SocialViewModelFactory
import com.umc_9th.sleepinghero.databinding.ActivityTimeSettingBinding
import com.umc_9th.sleepinghero.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ✅ 기기 내부 시간 설정 저장/불러오기
    private lateinit var settingManager: SettingManager

    // Home Repository & ViewModel
    private val homeRepository by lazy { HomeRepository(ApiClient.homeService) }
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(homeRepository) }

    // Character Repository & ViewModel
    private val characterRepository by lazy { CharacterRepository(ApiClient.characterService) }
    private val characterViewModel: CharacterViewModel by viewModels { CharacterViewModelFactory(characterRepository) }

    // Friend Repository & ViewModel
    private val friendRepository by lazy { FriendRepository(ApiClient.friendService) }
    private val friendViewModel: FriendViewModel by viewModels { FriendViewModelFactory(friendRepository) }

    // Sleep Repository & ViewModel (홈 통계/기록 조회는 유지)
    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }
    private val sleepViewModel: SleepViewModel by viewModels { SleepViewModelFactory(sleepRepository) }

    private val socialRepository by lazy {
        SocialRepository(ApiClient.socialService)
    }
    private val socialViewModel : SocialViewModel by viewModels(
        factoryProducer = { SocialViewModelFactory(socialRepository) }
    )


    // 데이터 캐시
    private var characterInfo: CharacterInfoResponse? = null
    private var dashboardData: DashBoardResponse? = null
    private var myRanking: Int? = null
    private var myHeroName: String? = null  // 영웅 이름 저장용

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // ✅ SettingManager 초기화 + 기본값 세팅 + 홈 표시
        settingManager = SettingManager(requireContext())
        if (settingManager.getSleepTime() == "null") settingManager.setSleepTime("11:00 PM")
        if (settingManager.getAwakeTime() == "null") settingManager.setAwakeTime("07:00 AM")

        binding.tvBedtimeValue.text = settingManager.getSleepTime()
        binding.tvWakeupValue.text = settingManager.getAwakeTime()

        setupButtons()
        observeData()
        observeSocial()

        val token = TokenManager.getAccessToken(requireContext())
        if (token != null) {
            socialViewModel.myCharacter(token)
            loadAllData()
        } else {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun setupButtons() {
        binding.btnDiary.setOnClickListener {
            Toast.makeText(requireContext(), "컨디션 화면 준비중", Toast.LENGTH_SHORT).show()
        }

        // ✅ 수면 시작하기 버튼: 서버 호출 없이 바로 SleepTracker로 이동 (기기 내부 시간 기준)
        binding.btnStartSleep.setOnClickListener {
            val sleepTime = settingManager.getSleepTime().takeIf { it != "null" } ?: "11:00 PM"
            val awakeTime = settingManager.getAwakeTime().takeIf { it != "null" } ?: "07:00 AM"

            val frag = SleepTrackerFragment.newInstance(sleepTime, awakeTime)

            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, frag)
                .addToBackStack(null)
                .commit()
        }

        binding.btnZoomIn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, MapFragment())
                .addToBackStack(null)
                .commit()
        }

        // ✅ 취침 시간 설정 (기기 내 저장 + API 연동)
        binding.bedtimeContainer.setOnClickListener {
            showTimePickerDialog(
                title = "취침 시간 설정",
                initialTime = binding.tvBedtimeValue.text.toString(),
                onConfirm = { finalStr ->
                    binding.tvBedtimeValue.text = finalStr
                    settingManager.setSleepTime(finalStr)
                    // API 연동: 목표 수면 시간 설정
                    requestSetSleepGoal()
                }
            )
        }

        // ✅ 기상 시간 설정 (기기 내 저장 + API 연동)
        binding.wakeupContainer.setOnClickListener {
            showTimePickerDialog(
                title = "기상 시간 설정",
                initialTime = binding.tvWakeupValue.text.toString(),
                onConfirm = { finalStr ->
                    binding.tvWakeupValue.text = finalStr
                    settingManager.setAwakeTime(finalStr)
                    // API 연동: 목표 수면 시간 설정
                    requestSetSleepGoal()
                }
            )
        }
    }

    private fun observeData() {
        characterViewModel.characterInfo.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                characterInfo = data
                updateCharacterUI()
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("HomeFragment", "캐릭터 정보 불러오기 실패 : $message")
            }
        }

        homeViewModel.homeDashboard.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                dashboardData = data
                updateDashboardUI()
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("HomeFragment", "홈 대시보드 불러오기 실패 : $message")
            }
        }

        friendViewModel.friendRanking.observe(viewLifecycleOwner) { result ->
            result.onSuccess { rankings ->
                val myNickname = characterInfo?.name
                myRanking = rankings.find { it.nickname == myNickname }?.rank
                updateRankingUI()
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("HomeFragment", "친구 랭킹 불러오기 실패 : $message")
            }
        }

        // ✅ 홈의 취침/기상 시간은 '설정값' 유지 (서버 기록으로 덮어쓰지 않음)
        sleepViewModel.sleepSessions.observe(viewLifecycleOwner) { result ->
            result.onSuccess { sessions ->
                binding.tvBedtimeValue.text =
                    settingManager.getSleepTime().takeIf { it != "null" } ?: "11:00 PM"
                binding.tvWakeupValue.text =
                    settingManager.getAwakeTime().takeIf { it != "null" } ?: "07:00 AM"

                if (sessions.content.isNotEmpty()) {
                    val latestSession = sessions.content.first()
                    binding.tvSleepGoalValue.text = calculateDurationText(latestSession.sleptTime, latestSession.wokeTime)

                    val totalHours = calculateTotalSleepHours(sessions.content)
                    binding.tvClockValue.text = "${totalHours}시간"
                } else {
                    binding.tvSleepGoalValue.text = "8시간"
                    binding.tvClockValue.text = "0시간"
                }
            }.onFailure {
                binding.tvBedtimeValue.text =
                    settingManager.getSleepTime().takeIf { it != "null" } ?: "11:00 PM"
                binding.tvWakeupValue.text =
                    settingManager.getAwakeTime().takeIf { it != "null" } ?: "07:00 AM"
                binding.tvSleepGoalValue.text = "8시간"
                binding.tvClockValue.text = "0시간"
            }
        }
    }

    private fun loadAllData() {
        val token = TokenManager.getAccessToken(requireContext())
        if (token != null) {
            characterViewModel.loadCharacterInfo(token)
            homeViewModel.loadHomeDashboard(token)
            friendViewModel.loadFriendRanking(token)
            sleepViewModel.loadSleepSessions(token, page = 0, size = 30)
        } else {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCharacterUI() {
        val character = characterInfo ?: return

        binding.tvUserLevelName.text = "LV.${character.currentLevel} ${character.name} 님"

        val expRemaining = character.needExp - character.currentExp
        binding.tvExpRemaining.text = "-$expRemaining EXP"

        val expPercentage = if (character.needExp > 0) {
            ((character.currentExp.toFloat() / character.needExp) * 100).toInt()
        } else 0

        updateExpBar(expPercentage)
    }

    private fun updateDashboardUI() {
        val data = dashboardData ?: return
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

    private fun formatTimeToAMPM(isoTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoTime)

            val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            outputFormat.timeZone = TimeZone.getDefault()
            outputFormat.format(date ?: Date()).uppercase(Locale.ENGLISH)
        } catch (e: Exception) {
            "00:00 AM"
        }
    }

    private fun calculateDurationText(sleptTime: String, wokeTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val sleptDate = inputFormat.parse(sleptTime)
            val wokeDate = inputFormat.parse(wokeTime)

            if (sleptDate != null && wokeDate != null) {
                val diffMillis = wokeDate.time - sleptDate.time
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60

                if (minutes > 0) "${hours}시간 ${minutes}분" else "${hours}시간"
            } else "8시간"
        } catch (e: Exception) {
            "8시간"
        }
    }

    private fun calculateTotalSleepHours(sessions: List<SleepSessionItem>): Int {
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
            0
        }
    }

    // -------------------------
    // ✅ 시간 설정 다이얼로그 (SettingFragment에서 필요한 부분만 추출)
    // -------------------------
    private fun showTimePickerDialog(
        title: String,
        initialTime: String,
        onConfirm: (String) -> Unit
    ) {
        val dialogBinding = ActivityTimeSettingBinding.inflate(layoutInflater)

        val time = parseTimeString(initialTime.ifBlank { "11:00 PM" })
        var hour = time.first
        var min = time.second
        var ampm = time.third

        dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
        dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
        dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(title)
            .create()

        dialogBinding.btnTimesetHourup.setOnClickListener {
            if (hour == 11) {
                hour = 12
                ampm = 1 - ampm
            } else if (hour == 12) {
                hour = 1
            } else {
                hour++
            }
            dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
            dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
        }

        dialogBinding.btnTimesetHourdown.setOnClickListener {
            if (hour == 12) {
                hour = 11
            } else if (hour == 1) {
                hour = 12
                ampm = 1 - ampm
            } else {
                hour--
            }
            dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
            dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
        }

        dialogBinding.btnTimesetMinup.setOnClickListener {
            min = if (min == 50) 0 else min + 10
            dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
        }

        dialogBinding.btnTimesetMindown.setOnClickListener {
            min = if (min == 0) 50 else min - 10
            dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
        }

        dialogBinding.btnTimesetAmpmA.setOnClickListener {
            ampm = 1 - ampm
            dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
        }
        dialogBinding.btnTimesetAmpmB.setOnClickListener {
            ampm = 1 - ampm
            dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)
        }

        dialogBinding.btnTimesetConfirm.setOnClickListener {
            val finalStr = "${makeTimeString(hour, 0)}:${makeTimeString(min, 0)} ${makeTimeString(ampm, 1)}"
            onConfirm(finalStr)
            dialog.dismiss()
        }
        dialogBinding.btnTimesetCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun parseTimeString(timeStr: String): Triple<Int, Int, Int> {
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

    private fun makeTimeString(time: Int, type: Int): String {
        return if (type == 1) {
            if (time == 0) "AM" else "PM"
        } else {
            if (time < 10) "0$time" else time.toString()
        }
    }

    // -------------------------
    // API 연동: 목표 수면 시간 설정
    // -------------------------
    private fun requestSetSleepGoal() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            if (token.isNullOrEmpty()) {
                Log.d("HomeFragment", "토큰이 없어 목표 수면 시간 설정 불가")
                return@launch
            }

            val sleepTimeStr = settingManager.getSleepTime().takeIf { it != "null" } ?: "11:00 PM"
            val wakeTimeStr = settingManager.getAwakeTime().takeIf { it != "null" } ?: "07:00 AM"

            // "11:00 PM" -> "23:00" 형식으로 변환
            val sleepTime24 = convertTo24HourFormat(sleepTimeStr)
            val wakeTime24 = convertTo24HourFormat(wakeTimeStr)

            val result = sleepRepository.setSleepGoal(token, sleepTime24, wakeTime24)

            result.onSuccess { response ->
                Log.d("HomeFragment", "목표 수면 시간 설정 성공: ${response.sleepTime} ~ ${response.wakeTime} (${response.totalMinutes}분)")
            }.onFailure { error ->
                Log.e("HomeFragment", "목표 수면 시간 설정 실패: ${error.message}")
            }
        }
    }

    /**
     * "11:00 PM" 형식을 "23:00" 형식으로 변환
     */
    private fun convertTo24HourFormat(timeStr: String): String {
        return try {
            val (hour, minute, ampm) = parseTimeString(timeStr)
            var hour24 = hour % 12
            if (ampm == 1) hour24 += 12  // PM이면 12시간 추가
            if (hour24 == 24) hour24 = 0  // 24시는 0시로
            String.format("%02d:%02d", hour24, minute)
        } catch (e: Exception) {
            Log.e("HomeFragment", "시간 형식 변환 실패: $timeStr", e)
            "23:00"  // 기본값
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeSocial() {
        socialViewModel.myCharResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                binding.tvExpRemaining.text = "-${data.needExp - data.currentExp} EXP"
                var per: Int = (data.currentExp.toFloat() / data.needExp.toFloat() * 100).toInt()
                binding.tvExpPercentage.text = "$per%"
                val progress = data.currentExp.toFloat() / data.needExp.toFloat()
                val params = binding.progressExp.layoutParams as ConstraintLayout.LayoutParams
                // OR more directly:
                params.matchConstraintPercentWidth = progress.coerceIn(0f, 1f)
                binding.progressExp.layoutParams = params

                val token = TokenManager.getAccessToken(requireContext())
                if (token != null) {
                    socialViewModel.charSearch(token, data.name)
                } else {
                    Log.d("HomeFragment", "토큰이 없어 charSearch 호출 불가")
                }
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("HomeFragment", "myCharResponse 불러오기 실패 : $message")
            }
        }
        socialViewModel.charSearchResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                binding.tvUserLevelName.text = "LV. ${data.level} ${data.heroName} 님"
                myHeroName = data.heroName  // 변수에 저장
                binding.tvCalendarValue.text = "${data.continuousSleepDays}일"
                binding.tvClockValue.text = "${data.totalSleepHour}시간"

                val token = TokenManager.getAccessToken(requireContext())
                if (token != null) {
                    socialViewModel.loadFriendRanking(token)
                } else {
                    Log.d("HomeFragment", "토큰이 없어 loadFriendRanking 호출 불가")
                }
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("HomeFragment", "charSearchResponse 불러오기 실패 : $message")
            }
        }
        socialViewModel.friendRankingResponse.observe(viewLifecycleOwner) {result ->
            result.onSuccess { data ->
                val rank = data.indexOfFirst { it.nickName == myHeroName } + 1
                binding.tvTrophyValue.text = "${rank}등"
            }.onFailure { error->
                val message = error.message ?: "알 수 없는 오류"
                Log.d("HomeFragment", "친구 불러오기 실패 : $message")
            }
        }
    }
}
