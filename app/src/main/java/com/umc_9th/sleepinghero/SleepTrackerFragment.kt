package com.umc_9th.sleepinghero

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    // (보조) 프래그먼트가 살아있는 동안 목표 알림 중복 방지
    private var goalNotifiedInUi: Boolean = false

    private val requestPostNotiPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                setNotiEnabled(true)
                applyNotiUi(true)
                showTrackingNotification()
                scheduleGoalAlarm()
            } else {
                setNotiEnabled(false)
                applyNotiUi(false)
                cancelTrackingNotification()
                cancelGoalAlarm()
            }
        }

    private val tickRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startMillis

            binding.tvTimer.text = formatElapsed(elapsed)

            val elapsedHours = elapsed / 1000.0 / 60.0 / 60.0
            val ratio = (elapsedHours / goalHours).coerceIn(0.0, 1.0)
            val percent = (ratio * 100.0).roundToInt()

            binding.tvProgressInfo.text = String.format("%.1f / %.1f 시간", elapsedHours, goalHours)
            binding.tvPercent.text = "${percent}%"
            binding.ivCircleProgress.rotation = (360f * ratio).toFloat()

            // 알림은 "설정 시간"에 AlarmManager로 울리지만,
            // 프래그먼트가 화면에 떠있는 상태에서라도 목표를 넘겼으면 1회는 보장(보조)
            if (!goalNotifiedInUi && isNotiEnabled() && elapsedHours >= goalHours) {
                goalNotifiedInUi = true
                triggerGoalReachedNotificationNow()
            }

            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

        // end API 결과 관찰(성공 시 ClearFragment 이동)
        observeEndResult()

        // 클릭들
        setupClicks()

        // 알림 상태 복원
        val enabled = isNotiEnabled()
        applyNotiUi(enabled)
        if (enabled) {
            ensureTrackingNotificationChannel()
            showTrackingNotification()
            scheduleGoalAlarm()
        } else {
            cancelGoalAlarm()
        }
    }

    private fun setupClicks() {
        // ✅ 수면 종료 버튼: 누르면 즉시 endSleep 호출 -> 성공 시 ClearFragment 이동
        // home_sleep_stopbutton이 이 버튼이라면 binding.btnStop만 실제 바인딩명으로 맞춰주면 됨.
        binding.btnStop.setOnClickListener {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw == null) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val auth = if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
        }

        // ✅ 화면 잠금
        binding.tvScreenLock.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, LockerFragment())
                .addToBackStack(null)
                .commit()
        }

        // ✅ 알림 설정 ON/OFF (추적 알림 + 목표 알림 스케줄)
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
                scheduleGoalAlarm()
            } else {
                setNotiEnabled(false)
                applyNotiUi(false)
                cancelTrackingNotification()
                cancelGoalAlarm()
            }
        }
    }

    private fun observeEndResult() {
        sleepViewModel.sleepEndResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                // 종료 시 알림/알람 정리
                setNotiEnabled(false)
                cancelTrackingNotification()
                cancelGoalAlarm()

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
            }
        }
    }

    // -------------------------
    // Tracking Notification
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

    private fun ensureTrackingNotificationChannel() {
        val nm = NotificationManagerCompat.from(requireContext())
        val channel = NotificationChannelCompat.Builder(
            TRACKING_NOTI_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName("Sleep Tracker")
            .setDescription("수면 추적 알림")
            .build()
        nm.createNotificationChannel(channel)
    }

    private fun showTrackingNotification() {
        ensureTrackingNotificationChannel()
        if (needsPostNotiPermission() && !hasPostNotiPermission()) return

        val noti = NotificationCompat.Builder(requireContext(), TRACKING_NOTI_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("수면 추적 중")
            .setContentText("수면 추적이 진행 중입니다.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        NotificationManagerCompat.from(requireContext()).notify(TRACKING_NOTI_ID, noti)
    }

    private fun cancelTrackingNotification() {
        NotificationManagerCompat.from(requireContext()).cancel(TRACKING_NOTI_ID)
    }

    // -------------------------
    // Goal Alarm: 설정 시간( start + goalHours )에 알림 울리기
    // -------------------------
    private fun scheduleGoalAlarm() {
        if (!isNotiEnabled()) return
        if (needsPostNotiPermission() && !hasPostNotiPermission()) return

        val triggerAt = startMillis + (goalHours * 60 * 60 * 1000).toLong()

        val pi = PendingIntent.getBroadcast(
            requireContext(),
            GOAL_ALARM_REQUEST_CODE,
            Intent(requireContext(), SleepGoalReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag()
        )

        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun cancelGoalAlarm() {
        val pi = PendingIntent.getBroadcast(
            requireContext(),
            GOAL_ALARM_REQUEST_CODE,
            Intent(requireContext(), SleepGoalReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag()
        )
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pi)
    }

    private fun pendingIntentImmutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    }

    private fun triggerGoalReachedNotificationNow() {
        if (needsPostNotiPermission() && !hasPostNotiPermission()) return
        SleepGoalReceiver.showGoalReachedNotification(requireContext())
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
        private const val TRACKING_NOTI_CHANNEL_ID = "sleep_tracker_channel"
        private const val TRACKING_NOTI_ID = 91001

        const val GOAL_NOTI_CHANNEL_ID = "sleep_goal_channel"
        const val GOAL_NOTI_ID = 91002

        private const val GOAL_ALARM_REQUEST_CODE = 92001
    }
}
