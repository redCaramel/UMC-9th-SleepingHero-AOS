package com.umc_9th.sleepinghero

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModel
import com.umc_9th.sleepinghero.api.viewmodel.SleepViewModelFactory
import com.umc_9th.sleepinghero.databinding.FragmentSleepTrackerBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SleepTrackerFragment : Fragment() {

    private var _binding: FragmentSleepTrackerBinding? = null
    private val binding get() = _binding!!

    // Sleep Repository & ViewModel
    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }
    private val sleepViewModel: SleepViewModel by viewModels { SleepViewModelFactory(sleepRepository) }

    // 타이머
    private val handler = Handler(Looper.getMainLooper())
    private var startMillis: Long = 0L

    // HomeFragment에서 전달받는 설정값
    private var sleepTimeStr: String = "11:00 PM"
    private var awakeTimeStr: String = "07:00 AM"

    // 목표(취침~기상) 시간(시간 단위)
    private var goalHours: Double = 8.0

    // 다음 기상 알람 시각(ms)
    private var nextWakeAtMillis: Long = 0L

    // 수면 시작 API에서 받은 recordId (ClearFragment 리뷰용)
    private var currentRecordId: Int = 0

    // 알림 토글 저장(추적 알림 UI용)
    private val prefsName = "sleep_tracker_prefs"
    private val keyNotiEnabled = "noti_enabled"
    private val keyStartMillis = "start_millis"  // 타이머 시작 시간 저장용

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

            // 진행률(목표 = 취침~기상 시간)
            val elapsedHours = elapsed / 1000.0 / 60.0 / 60.0
            val ratio = (elapsedHours / goalHours).coerceIn(0.0, 1.0)
            val percent = (ratio * 100.0).roundToInt()

            binding.tvProgressInfo.text = String.format("%.1f / %.1f 시간", elapsedHours, goalHours)
            binding.tvPercent.text = "${percent}%"

            // 원형 진행(회전으로 표현)
            binding.ivCircleProgress.rotation = (360f * ratio).toFloat()

            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            sleepTimeStr = it.getString(ARG_SLEEP_TIME) ?: "11:00 PM"
            awakeTimeStr = it.getString(ARG_AWAKE_TIME) ?: "07:00 AM"
        }

        // 목표 시간(취침~기상) 계산
        goalHours = computeGoalHours(sleepTimeStr, awakeTimeStr)

        // 다음 기상 시각 계산
        nextWakeAtMillis = computeNextWakeMillis(awakeTimeStr)
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

        // HomeFragment에서 넘긴 취침/기상 시간 UI에 표시
        binding.tvTimeRange.text = "$sleepTimeStr - $awakeTimeStr"

        // API 연동: 목표 설정 성공 후 수면 시작 (순서 보장으로 서버 400 방지)
        observeSleepStartResult()
        requestSetSleepGoalThenStart()

        // 타이머 시작 시간 복원 또는 새로 시작
        val savedStartMillis = getSavedStartMillis()
        startMillis = if (savedStartMillis > 0L) {
            savedStartMillis  // 저장된 시작 시간이 있으면 사용
        } else {
            val currentTime = System.currentTimeMillis()
            saveStartMillis(currentTime)  // 없으면 새로 시작하고 저장
            currentTime
        }

        handler.post(tickRunnable)

        // ✅ 앱이 꺼져 있어도 울리게: AlarmManager로 기상 알람 예약
        scheduleWakeAlarm(requireContext(), nextWakeAtMillis)

        // 클릭들
        setupClicks()

        // 추적 알림 상태 복원
        val enabled = isNotiEnabled()
        applyNotiUi(enabled)
        if (enabled) {
            ensureNotificationChannel()
            showTrackingNotification()
        }

        // (선택) Android 12+ 정확 알람 권한 안내
        maybeGuideExactAlarmPermission()
    }

    // -------------------------
    // API 연동: 목표 수면 시간 설정 후 수면 시작 (순차 호출로 서버 검증 통과)
    // -------------------------
    private fun requestSetSleepGoalThenStart() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val sleepTime24 = convertTo24HourFormat(sleepTimeStr)
            val wakeTime24 = convertTo24HourFormat(awakeTimeStr)

            val goalResult = sleepRepository.setSleepGoal(token, sleepTime24, wakeTime24)
            goalResult.onSuccess {
                sleepViewModel.startSleep(token)
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    "수면 목표 설정에 실패했습니다. 홈에서 취침/기상 시간을 설정한 뒤 다시 시도해 주세요.",
                    Toast.LENGTH_LONG
                ).show()
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
            "23:00"  // 기본값
        }
    }

    // -------------------------
    // API 연동: 수면 시작 (파라미터 없음. 서버가 현재 시간을 sleepTime으로 기록)
    // -------------------------
    private fun requestStartSleep() {
        val token = TokenManager.getAccessToken(requireContext())
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }
        sleepViewModel.startSleep(token)
    }

    private fun observeSleepStartResult() {
        sleepViewModel.sleepStartResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { data ->
                currentRecordId = data.recordId
            }.onFailure { error ->
                val msg = error.message ?: ""
                val userMessage = if (msg.contains("목표") || msg.contains("SLEEP404")) {
                    "수면 목표가 설정되지 않았습니다. 홈에서 취침/기상 시간을 먼저 설정해 주세요."
                } else {
                    "수면 시작 실패: $msg"
                }
                Toast.makeText(requireContext(), userMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClicks() {
        // 수면 종료(중단) 버튼: 알람 취소 + ClearFragment로 이동(임시 값)
        binding.btnStop.setOnClickListener {
            cancelWakeAlarm(requireContext())
            setNotiEnabled(false)
            applyNotiUi(false)
            cancelTrackingNotification()

            // 타이머 정리 및 시작 시간 초기화
            handler.removeCallbacks(tickRunnable)
            clearSavedStartMillis()

            val elapsedMinutes = ((System.currentTimeMillis() - startMillis) / 1000 / 60).toInt()

            val clearFragment = ClearFragment.newInstance(
                recordId = currentRecordId,
                durationMinutes = elapsedMinutes,
                gainedExp = 0,
                currentLevel = 0,
                currentExp = 0,
                needExp = 0,
                sleepTimeStr = sleepTimeStr,
                awakeTimeStr = awakeTimeStr
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, clearFragment)
                .addToBackStack(null)
                .commit()
        }

        // 화면 잠금 설정 -> LockerFragment로 이동(기존 유지)
        binding.tvScreenLock.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_main, LockerFragment())
                .addToBackStack(null)
                .commit()
        }

        // 추적 알림 ON/OFF
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

    // -------------------------
    // ✅ Wake Alarm (앱이 꺼져 있어도 울리게)
    // -------------------------

    private fun scheduleWakeAlarm(context: Context, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pi = PendingIntent.getBroadcast(
            context,
            WAKE_ALARM_REQ_CODE,
            Intent(context, WakeAlarmReceiver::class.java).apply { action = ACTION_WAKE_ALARM },
            pendingIntentFlags()
        )

        val canExact = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            // ✅ 가능하면 정확 알람
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            // ✅ 불가능하면 크래시 방지 + 대체 스케줄링(정확도는 떨어질 수 있음)
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)

            Toast.makeText(
                context,
                "정확 알람 권한이 없어 알람이 다소 늦을 수 있습니다. 설정에서 '정확한 알람'을 허용해 주세요.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cancelWakeAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context,
            WAKE_ALARM_REQ_CODE,
            Intent(context, WakeAlarmReceiver::class.java).apply { action = ACTION_WAKE_ALARM },
            pendingIntentFlags()
        )
        am.cancel(pi)
    }

    private fun computeNextWakeMillis(awakeTime: String): Long {
        val (h12, m, isPm) = parseTimeString(awakeTime)
        val calendar = Calendar.getInstance()

        // 12시간제 -> 24시간제
        var hour24 = h12 % 12
        if (isPm == 1) hour24 += 12

        calendar.set(Calendar.HOUR_OF_DAY, hour24)
        calendar.set(Calendar.MINUTE, m)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val now = System.currentTimeMillis()
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DATE, 1)
        }
        return calendar.timeInMillis
    }

    private fun computeGoalHours(sleepTime: String, awakeTime: String): Double {
        // 목표시간 = 취침시각~기상시각 (다음날 넘어가는 경우 포함)
        val (sh, sm, spm) = parseTimeString(sleepTime)
        val (ah, am, apm) = parseTimeString(awakeTime)

        fun toMinutes(h12: Int, m: Int, pm: Int): Int {
            var h24 = h12 % 12
            if (pm == 1) h24 += 12
            return h24 * 60 + m
        }

        val sleepMin = toMinutes(sh, sm, spm)
        val awakeMin = toMinutes(ah, am, apm)

        val diffMin = if (awakeMin >= sleepMin) {
            awakeMin - sleepMin
        } else {
            (24 * 60 - sleepMin) + awakeMin
        }

        return (diffMin / 60.0).coerceAtLeast(0.5)
    }

    private fun parseTimeString(timeStr: String): Triple<Int, Int, Int> {
        // "11:00 PM" -> (11, 0, 1)
        return try {
            val parts = timeStr.trim().split(" ")
            val time = parts.getOrNull(0) ?: "07:00"
            val ampm = parts.getOrNull(1) ?: "AM"
            val hm = time.split(":")

            val hour = hm.getOrNull(0)?.toIntOrNull() ?: 7
            val minute = hm.getOrNull(1)?.toIntOrNull() ?: 0
            val ampmFlag = if (ampm.equals("PM", ignoreCase = true)) 1 else 0

            Triple(hour, minute, ampmFlag)
        } catch (e: Exception) {
            Triple(7, 0, 0)
        }
    }

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun maybeGuideExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (am.canScheduleExactAlarms()) return

        // 정확 알람 권한 설정 화면으로 유도(강제 X)
        // 필요하면 버튼으로 유도하고 싶으면 UI에 연결하면 됨
    }

    // -------------------------
    // Notification ON/OFF (추적 알림)
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

        // Android 13+ 권한 없으면 바로 종료
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return
        }

        val noti = NotificationCompat.Builder(requireContext(), NOTI_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("수면 추적 중")
            .setContentText("수면 추적이 진행 중입니다.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        try {
            NotificationManagerCompat.from(requireContext()).notify(NOTI_ID, noti)
        } catch (se: SecurityException) {
            // 일부 기기/정책에서 예외 가능성 있어서 방어
        }
    }

    private fun cancelTrackingNotification() {
        NotificationManagerCompat.from(requireContext()).cancel(NOTI_ID)
    }

    // -------------------------
    // 타이머 시작 시간 저장/복원 (백그라운드에서도 계속 실행되도록)
    // -------------------------
    private fun saveStartMillis(millis: Long) {
        requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putLong(keyStartMillis, millis)
            .apply()
    }

    private fun getSavedStartMillis(): Long {
        return requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getLong(keyStartMillis, 0L)
    }

    private fun clearSavedStartMillis() {
        requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .remove(keyStartMillis)
            .apply()
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

    override fun onPause() {
        super.onPause()
        // onPause에서는 타이머를 멈추지 않음 (다른 Fragment로 가도 계속 실행)
        // 단지 UI 업데이트만 멈춤
    }

    override fun onResume() {
        super.onResume()
        handler.post(tickRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(tickRunnable)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Fragment가 완전히 destroy될 때만 시작 시간 초기화
        // (수면 종료 시에만 호출됨)
    }

    companion object {
        private const val NOTI_CHANNEL_ID = "sleep_tracker_channel"
        private const val NOTI_ID = 91001

        private const val ARG_SLEEP_TIME = "arg_sleep_time"
        private const val ARG_AWAKE_TIME = "arg_awake_time"

        // Wake alarm
        private const val WAKE_ALARM_REQ_CODE = 77701
        const val ACTION_WAKE_ALARM = "com.umc_9th.sleepinghero.ACTION_WAKE_ALARM"

        fun newInstance(sleepTime: String, awakeTime: String): SleepTrackerFragment {
            return SleepTrackerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SLEEP_TIME, sleepTime)
                    putString(ARG_AWAKE_TIME, awakeTime)
                }
            }
        }
    }
}

/**
 * ✅ 기상 알람 수신기
 * - 앱이 꺼져 있어도 동작(AlarmManager)
 * - 수신하면 잠금화면 위에 AlarmLockActivity를 띄워서 계속 울리게 함
 *
 * ⚠️ 반드시 AndroidManifest.xml에 receiver 등록 필요
 */
class WakeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != SleepTrackerFragment.ACTION_WAKE_ALARM) return

        val i = Intent(context, AlarmLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(i)
    }
}

/**
 * ✅ 알람이 꺼질 때까지 잠금 유지(잠금화면 위에 표시)
 * - 화면을 깨우고(가능한 범위 내) 잠금화면 위에 UI를 올림
 * - 버튼을 누르기 전까지 알람이 계속 울림
 *
 * ⚠️ 반드시 AndroidManifest.xml에 activity 등록 필요
 */
class AlarmLockActivity : ComponentActivity() {

    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면 위에 표시 + 화면 켜기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 간단 UI(코드로 생성)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "기상 알람"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val sub = TextView(this).apply {
            text = "알람이 울리고 있습니다. 끄기 버튼을 누르세요."
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val btn = Button(this).apply {
            text = "알람 끄기"
            setOnClickListener {
                stopAlarm()
                finish()
            }
        }

        root.addView(title)
        root.addView(sub)
        root.addView(btn)

        setContentView(root)

        startAlarm()
    }

    private fun startAlarm() {
        // 기본 알람 사운드(없으면 notification)
        val alarmUri: Uri =
            Settings.System.DEFAULT_ALARM_ALERT_URI
                ?: Settings.System.DEFAULT_NOTIFICATION_URI

        try {
            player = MediaPlayer().apply {
                setDataSource(this@AlarmLockActivity, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "알람 재생 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAlarm() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
