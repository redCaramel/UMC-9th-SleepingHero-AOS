package com.umc_9th.sleepinghero

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!trackingStarted) return

            val elapsedMillis = System.currentTimeMillis() - startMillis
            val elapsedMinutes = (elapsedMillis / 1000 / 60).toInt()

            binding.tvTimer.text = formatElapsed(elapsedMillis)

            val ratio = (elapsedMinutes.toDouble() / goalMinutes.toDouble()).coerceIn(0.0, 1.0)
            val percent = (ratio * 100.0).roundToInt()
            binding.tvPercent.text = "${percent}%"
            binding.tvProgressInfo.text = "${elapsedMinutes} / ${goalMinutes} 분"
            binding.ivCircleProgress.rotation = (360f * ratio).toFloat()

            if (!alarmShown && elapsedMinutes >= goalMinutes) {
                alarmShown = true
                showAlarmDialog()
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
        binding.tvProgressInfo.text = "0 / ${goalMinutes} 분"
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
            goToClear()
        }

        // ✅ 핵심: 여기서 startSleep 쳐서 recordId 받아두고, 성공하면 타이머 시작
        startSleepAndTracking()
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
                currentRecordId = data.recordId   // ✅ 저장
                startTracking()                   // ✅ 그 다음 타이머 시작
            }.onFailure { e ->
                Toast.makeText(requireContext(), "수면 시작 실패: ${e.message}", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun startTracking() {
        if (trackingStarted) return
        trackingStarted = true
        alarmShown = false
        startMillis = System.currentTimeMillis()

        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
    }

    private fun stopTracking() {
        trackingStarted = false
        handler.removeCallbacks(tickRunnable)
    }

    private fun showAlarmDialog() {
        if (parentFragmentManager.findFragmentByTag("AlarmDialog") != null) return
        AlarmDialogFragment().show(parentFragmentManager, "AlarmDialog")
    }

    private fun goToClear() {
        val elapsedMinutes = ((System.currentTimeMillis() - startMillis) / 1000 / 60).toInt()
        stopTracking()

        // ✅ 여기서 recordId = 0 박지 말고 start에서 받은 거 넘김
        if (currentRecordId == 0) {
            Toast.makeText(requireContext(), "recordId가 없습니다. start API가 먼저 성공해야 합니다.", Toast.LENGTH_LONG).show()
            return
        }

        val clearFragment = ClearFragment.newInstance(
            recordId = currentRecordId,   // ✅ 이게 핵심
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(tickRunnable)
        _binding = null
    }

    companion object {
        private const val ARG_SLEEP_TIME = "arg_sleep_time"
        private const val ARG_AWAKE_TIME = "arg_awake_time"
        private const val ARG_GOAL_MINUTES = "arg_goal_minutes"

        fun newInstance(sleepTime: String, awakeTime: String, goalMinutes: Int = 1): SleepTrackerFragment {
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
