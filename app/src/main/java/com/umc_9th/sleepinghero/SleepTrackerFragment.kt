package com.umc_9th.sleepinghero

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentSleepTrackerBinding
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SleepTrackerFragment : Fragment() {

    private var _binding: FragmentSleepTrackerBinding? = null
    private val binding get() = _binding!!

    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }
    private val sleepViewModel: SleepViewModel by viewModels { SleepViewModelFactory(sleepRepository) }

    // 타이머
    private val handler = Handler(Looper.getMainLooper())
    private var startMillis: Long = 0L

    // 목표 수면시간 (Routine 파트가 아직이면 임시)
    private val goalHours = 8.5

    // 알림 토글 저장
    private val prefsName = "sleep_tracker_prefs"
    private val keyNotiEnabled = "noti_enabled"

    private val requestPostNotiPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                setNotiEnabled(true)
                applyNotiUi(true)
                showTrackingNotification()
                Toast.makeText(requireContext(), "알림 ON", Toast.LENGTH_SHORT).show()
            } else {
                setNotiEnabled(false)
                applyNotiUi(false)
                Toast.makeText(requireContext(), "알림 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }

    private val tickRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startMillis

            // 00 : 00 : 01
            binding.tvTimer.text = formatElapsed(elapsed)

            // 0.0 / 8.5 시간, 0%
            val elapsedHours = elapsed / 1000.0 / 60.0 / 60.0
            val ratio = (elapsedHours / goalHours).coerceIn(0.0, 1.0)
            val percent = (ratio * 100.0).roundToInt()

            binding.tvProgressInfo.text = String.format("%.1f / %.1f 시간", elapsedHours, goalHours)
            binding.tvPercent.text = "${percent}%"

            // 원형 진행률(리소스가 원형 기준이면 회전으로 대충 표현 가능)
            binding.ivCircleProgress.rotation = (360f * ratio).toFloat()

            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 타이머 시작
        startMillis = System.currentTimeMillis()
        handler.post(tickRunnable)

        // SleepStopDialog 결과 리스너(중단/계속)
        observeStopDialogResult()

        // end API 결과 관찰(성공 시 ClearFragment 이동)
        observeEndResult()

        // 클릭들
        setupClicks()

        // 알림 상태 복원
        val enabled = isNotiEnabled()
        applyNotiUi(enabled)
        if (enabled) {
            ensureNotificationChannel()
            showTrackingNotification()
        }
    }

    private fun setupClicks() {
        // ✅ 수면 종료 버튼: 바로 종료 X -> SleepStopFragment(오버레이) 띄우기
        binding.btnStop.setOnClickListener {
            // SleepStopFragment는 DialogFragment로 구현돼 있어야 함
            SleepStopFragment().show(childFragmentManager, "SleepStopDialog")
        }

        // ✅ 화면 잠금 설정 -> fragment_locker로 이동
        binding.tvScreenLock.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, LockerFragment())
                .addToBackStack(null)
                .commit()
        }

        // ✅ 알림 설정 ON/OFF (로컬 저장 + 실제 알림 표시/해제)
        binding.btnAlarm.setOnClickListener {
            val next = !isNotiEnabled()

            if (next) {
                if (needsPostNotiPermission() && !hasPostNotiPermission()) {
                    requestPostNotiPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnClickListener
                }
                setNotiEnabled(true)
                applyNotiUi(true)
                showTrackingNotification()
                Toast.makeText(requireContext(), "알림 ON", Toast.LENGTH_SHORT).show()
            } else {
                setNotiEnabled(false)
                applyNotiUi(false)
                cancelTrackingNotification()
                Toast.makeText(requireContext(), "알림 OFF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * SleepStopFragment(Dialog)에서:
     * - "계속" 누르면 dismiss만
     * - "중단" 누르면 end API 호출 -> 성공 시 ClearFragment 이동
     *
     * SleepStopFragment는 setFragmentResult로 결과를 넘겨야 함.
     * (키/액션 값은 아래와 맞춰야 함)
     */
    private fun observeStopDialogResult() {
        childFragmentManager.setFragmentResultListener(
            SleepStopFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString("action")) {
                "resume" -> {
                    // 아무 것도 안 함 (그냥 계속)
                }

                "stop" -> {
                    val token = TokenManager.getAccessToken(requireContext())
                    if (token == null) {
                        Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                        return@setFragmentResultListener
                    }

                    // 중복 클릭 방지
                    binding.btnStop.isEnabled = false
                    Toast.makeText(requireContext(), "수면 종료 중...", Toast.LENGTH_SHORT).show()

                    // ✅ end API 호출
                    sleepViewModel.endSleep(token)
                }
            }
        }
    }

    /**
     * end API 성공 -> fragment_clear(ClearFragment)로 이동해서 결과 표시
     */
    private fun observeEndResult() {
        sleepViewModel.sleepEndResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                // 추적 알림 끄기
                setNotiEnabled(false)
                applyNotiUi(false)
                cancelTrackingNotification()

                // ✅ 개선된 방식: ClearFragment.newInstance() 사용
                val clearFragment = ClearFragment.newInstance(
                    recordId = data.recordId,
                    durationMinutes = data.durationMinutes,
                    gainedExp = data.sleepReward.gainedExp,
                    currentLevel = data.sleepReward.levelChange?.currentLevel ?: 0,
                    currentExp = data.sleepReward.levelChange?.currentExp ?: 0,
                    needExp = data.sleepReward.levelChange?.needExp ?: 0
                )

                parentFragmentManager.beginTransaction()
                    .replace(R.id.container_main, clearFragment)
                    .addToBackStack(null)
                    .commit()
            }.onFailure { e ->
                binding.btnStop.isEnabled = true
                Toast.makeText(requireContext(), "수면 종료 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // -------------------------
    // Notification ON/OFF
    // -------------------------
    private fun isNotiEnabled(): Boolean {
        return requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getBoolean(keyNotiEnabled, false)
    }

    private fun setNotiEnabled(enabled: Boolean) {
        requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(keyNotiEnabled, enabled)
            .apply()
    }

    private fun applyNotiUi(enabled: Boolean) {
        // btn_alarm(FrameLayout) 안 TextView(알람 설정 텍스트)를 찾아 텍스트만 바꿈
        findInnerTextView(binding.btnAlarm)?.text = if (enabled) "알림 ON" else "알림 OFF"
        binding.btnAlarm.alpha = if (enabled) 1.0f else 0.75f
    }

    private fun findInnerTextView(container: ViewGroup): TextView? {
        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i)
            if (v is TextView) return v
        }
        return null
    }

    private fun needsPostNotiPermission(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun hasPostNotiPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureNotificationChannel() {
        val nm = NotificationManagerCompat.from(requireContext())
        val channel = NotificationChannelCompat.Builder(
            NOTI_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName("Sleep Tracker")
            .setDescription("수면 추적 알림")
            .build()
        nm.createNotificationChannel(channel)
    }

    private fun showTrackingNotification() {
        ensureNotificationChannel()
        if (needsPostNotiPermission() && !hasPostNotiPermission()) return

        val noti = NotificationCompat.Builder(requireContext(), NOTI_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("수면 추적 중")
            .setContentText("수면 추적이 진행 중입니다.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        NotificationManagerCompat.from(requireContext()).notify(NOTI_ID, noti)
    }

    private fun cancelTrackingNotification() {
        NotificationManagerCompat.from(requireContext()).cancel(NOTI_ID)
    }

    // -------------------------
    // Utils
    // -------------------------
    private fun formatElapsed(millis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format("%02d : %02d : %02d", h, m, s)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(tickRunnable)
        _binding = null
    }

    companion object {
        private const val NOTI_CHANNEL_ID = "sleep_tracker_channel"
        private const val NOTI_ID = 91001
    }
}
