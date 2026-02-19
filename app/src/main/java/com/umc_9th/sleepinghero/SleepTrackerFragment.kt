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
import androidx.lifecycle.lifecycleScope
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.databinding.FragmentSleepTrackerBinding
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SleepTrackerFragment : Fragment() {

    private var _binding: FragmentSleepTrackerBinding? = null
    private val binding get() = _binding!!

    // ✅ 추가: repository
    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }

    // ✅ 추가: start에서 받은 recordId 저장용
    private var currentRecordId: Int = 0

    // 타이머
    private val handler = Handler(Looper.getMainLooper())
    private var startMillis: Long = 0L

    private var sleepTimeStr: String = "11:00 PM"
    private var awakeTimeStr: String = "07:00 AM"
    private var goalMinutes: Int = 1

    private var trackingStarted = false
    private var alarmShown = false

    // -------------------------
    // Notification ON/OFF
    // -------------------------
    private val prefsName = "sleep_tracker_prefs"
    private val keyNotiEnabled = "noti_enabled"

    // ✅ tracking 복원용
    private val keyTrackingActive = "tracking_active"
    private val keyTrackingStartMillis = "tracking_start_millis"
    private val keyTrackingRecordId = "tracking_record_id"

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
            if (!trackingStarted) return

            val elapsedMillis = System.currentTimeMillis() - startMillis
            val elapsedMinutes = (elapsedMillis / 1000 / 60).toInt()

            binding.tvTimer.text = formatElapsed(elapsedMillis)

            val ratio = (elapsedMinutes.toDouble() / goalMinutes.toDouble()).coerceIn(0.0, 1.0)
            val percent = (ratio * 100.0).roundToInt()
            binding.tvPercent.text = "${percent}%"
            binding.tvProgressInfo.text =
                "${minutesToRoundedHours(elapsedMinutes)} / ${minutesToRoundedHours(goalMinutes)}시간"
            binding.ivCircleProgress.rotation = (360f * ratio).toFloat()

            // ✅ 핵심 수정: "알람 ON(=noti enabled)"일 때만 알람 다이얼로그 뜨게
            if (!alarmShown && elapsedMinutes >= goalMinutes) {
                alarmShown = true
                showAlarmDialog(playSound = isNotiEnabled())
            }

            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            sleepTimeStr = it.getString(ARG_SLEEP_TIME) ?: sleepTimeStr
            awakeTimeStr = it.getString(ARG_AWAKE_TIME) ?: awakeTimeStr
            goalMinutes = it.getInt(ARG_GOAL_MINUTES, goalMinutes)
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

        binding.tvTimeRange.text = "$sleepTimeStr - $awakeTimeStr"
        binding.tvTimer.text = "00 : 00 : 00"
        binding.tvPercent.text = "0%"
        binding.tvProgressInfo.text = "0.0 / ${minutesToRoundedHours(goalMinutes)}시간"
        binding.ivCircleProgress.rotation = 0f

        parentFragmentManager.setFragmentResultListener(
            "ALARM_DISMISSED",
            viewLifecycleOwner
        ) { _, _ ->
            goToClear()
        }

        binding.btnStop.setOnClickListener {
            if (!trackingStarted) {
                Toast.makeText(requireContext(), "수면이 시작되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showSleepStopDialog()
        }

        parentFragmentManager.setFragmentResultListener(
            SleepStopFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString("action")) {
                SleepStopFragment.ACTION_STOP -> {
                    goToClear()
                }
                SleepStopFragment.ACTION_RESUME -> {
                    // 그대로 유지
                }
            }
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

        // ✅ 알림 상태 복원
        val enabled = isNotiEnabled()
        applyNotiUi(enabled)
        if (enabled) {
            ensureNotificationChannel()
            showTrackingNotification()
        }

        // ✅ 추가: tracking 복원
        if (isTrackingActive()) {
            currentRecordId = getTrackingRecordId()
            startMillis = getTrackingStartMillis()
            // startMillis이 0이면 복원 실패 -> 그냥 기존 로직 타게
            if (currentRecordId != 0 && startMillis != 0L) {
                trackingStarted = true
                alarmShown = false
                handler.removeCallbacks(tickRunnable)
                handler.post(tickRunnable)
                return
            } else {
                clearTrackingState()
            }
        }

        // ✅ 핵심: 여기서 startSleep 쳐서 recordId 받아두고, 성공하면 타이머 시작
        startSleepAndTracking()
    }

    private fun showSleepStopDialog() {
        if (parentFragmentManager.findFragmentByTag("SleepStopDialog") != null) return
        SleepStopFragment().show(parentFragmentManager, "SleepStopDialog")
    }

    private fun minutesToRoundedHours(min: Int): String {
        val hours = min / 60.0
        return String.format("%.1f", hours)
    }

    // ✅ 추가: start API 호출해서 recordId 저장
    private fun startSleepAndTracking() {
        val token = TokenManager.getAccessToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val result = sleepRepository.startSleep(token)

            result.onSuccess { data ->
                currentRecordId = data.recordId
                startTracking()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "목표 시간과 현재 시간이 다릅니다: ${e.message}", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun startTracking() {
        if (trackingStarted) return
        trackingStarted = true
        alarmShown = false
        startMillis = System.currentTimeMillis()

        // ✅ tracking 상태 저장
        setTrackingActive(true)
        setTrackingStartMillis(startMillis)
        setTrackingRecordId(currentRecordId)

        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
    }

    private fun stopTracking() {
        trackingStarted = false
        handler.removeCallbacks(tickRunnable)

        // ✅ tracking 상태 해제
        clearTrackingState()
    }

    private fun showAlarmDialog(playSound: Boolean) {
        if (parentFragmentManager.findFragmentByTag("AlarmDialog") != null) return
        AlarmDialogFragment.newInstance(playSound).show(parentFragmentManager, "AlarmDialog")
    }

    private fun goToClear() {
        val elapsedMinutes = ((System.currentTimeMillis() - startMillis) / 1000 / 60).toInt()
        stopTracking()

        if (currentRecordId == 0) {
            Toast.makeText(requireContext(), "recordId가 없습니다. start API가 먼저 성공해야 합니다.", Toast.LENGTH_LONG).show()
            return
        }

        // 추적 알림 끄기
        setNotiEnabled(false)
        applyNotiUi(false)
        cancelTrackingNotification()

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
            .commitAllowingStateLoss()
    }

    private fun formatElapsed(millis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format("%02d : %02d : %02d", h, m, s)
    }

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

    private fun needsPostNotiPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

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
    // tracking state (prefs)
    // -------------------------
    private fun isTrackingActive(): Boolean {
        return requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getBoolean(keyTrackingActive, false)
    }

    private fun setTrackingActive(active: Boolean) {
        requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(keyTrackingActive, active)
            .apply()
    }

    private fun setTrackingStartMillis(millis: Long) {
        requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putLong(keyTrackingStartMillis, millis)
            .apply()
    }

    private fun getTrackingStartMillis(): Long {
        return requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getLong(keyTrackingStartMillis, 0L)
    }

    private fun setTrackingRecordId(recordId: Int) {
        requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putInt(keyTrackingRecordId, recordId)
            .apply()
    }

    private fun getTrackingRecordId(): Int {
        return requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getInt(keyTrackingRecordId, 0)
    }

    private fun clearTrackingState() {
        requireContext()
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(keyTrackingActive, false)
            .putLong(keyTrackingStartMillis, 0L)
            .putInt(keyTrackingRecordId, 0)
            .apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(tickRunnable)
        _binding = null
    }

    companion object {
        private const val ARG_SLEEP_TIME = "arg_sleep_time"
        private const val ARG_AWAKE_TIME = "arg_awake_time"
        private const val ARG_GOAL_MINUTES = "arg_goal_minutes"

        private const val NOTI_CHANNEL_ID = "sleep_tracker_channel"
        private const val NOTI_ID = 91001

        fun newInstance(
            sleepTime: String,
            awakeTime: String,
            goalMinutes: Int = 1
        ): SleepTrackerFragment {
            return SleepTrackerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SLEEP_TIME, sleepTime)
                    putString(ARG_AWAKE_TIME, awakeTime)
                    putInt(ARG_GOAL_MINUTES, goalMinutes)
                }
            }
        }
    }
}
