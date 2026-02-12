package com.umc_9th.sleepinghero

import android.app.Activity
import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.umc_9th.sleepinghero.databinding.FragmentLockerBinding

class LockerFragment : Fragment() {

    private var _binding: FragmentLockerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔓 화면 잠금 해제 → LockTask 종료 + SleepTrackerFragment로 복귀
        binding.btnUnlock.setOnClickListener {
            stopAppPinningIfRunning()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()

        // ✅ 화면 꺼짐 방지(잠금 유지 느낌)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        startAppPinningIfPossible()
    }

    override fun onPause() {
        super.onPause()
        // 화면 유지 플래그는 필요하면 유지해도 됨. 여기서는 잠금 화면에서만 유지하려고 제거.
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startAppPinningIfPossible() {
        val activity = activity ?: return

        // 이미 LockTask 중이면 중복 호출 방지
        if (isInLockTaskMode(activity)) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // startLockTask()가 성공하려면:
                // 1) Device Owner로 allowlist 되었거나
                // 2) 사용자가 시스템에서 "화면 고정"을 켜고, 현재 앱을 고정하는 흐름이 허용되어야 함
                activity.startLockTask()
            }
        } catch (t: Throwable) {
            // 기기 설정/정책상 막히면 여기로 옴
            Toast.makeText(
                requireContext(),
                "이 기기에서는 '앱 고정(화면 고정)'을 사용할 수 없습니다. 설정에서 '화면 고정'을 켜야 할 수 있습니다.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun stopAppPinningIfRunning() {
        val activity = activity ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (isInLockTaskMode(activity)) {
                    activity.stopLockTask()
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun isInLockTaskMode(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val am = activity.getSystemService(ActivityManager::class.java)
            am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            @Suppress("DEPRECATION")
            val am = activity.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            am.isInLockTaskMode
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
