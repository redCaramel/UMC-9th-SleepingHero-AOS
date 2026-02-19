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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.umc_9th.sleepinghero.databinding.FragmentLockerBinding

class LockerFragment : Fragment() {

    private var _binding: FragmentLockerBinding? = null
    private val binding get() = _binding!!

    // 시스템 바 컨트롤러
    private var insetsController: WindowInsetsControllerCompat? = null

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

        // 🔓 화면 잠금 해제 → LockTask 종료 + 뒤로가기(=SleepTrackerFragment로 복귀)
        binding.btnUnlock.setOnClickListener {
            stopAppPinningIfRunning()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()

        // ✅ 화면 꺼짐 방지
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ✅ 하단 BottomNavigation 숨김 (ActivityMainBinding 접근)
        (activity as? MainActivity)?.setBottomNavVisible(false)

        // ✅ 시스템 네비/상태바 숨김
        val window = requireActivity().window
        val decorView = window.decorView
        insetsController = WindowInsetsControllerCompat(window, decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        startAppPinningIfPossible()
    }

    override fun onPause() {
        super.onPause()

        // ✅ 잠금 화면에서만 유지하려고 제거
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ✅ 시스템 바 복구
        insetsController?.show(WindowInsetsCompat.Type.systemBars())
        insetsController = null

        // ✅ BottomNavigation 복구
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    private fun startAppPinningIfPossible() {
        val activity = activity ?: return
        if (isInLockTaskMode(activity)) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startLockTask()
            }
        } catch (t: Throwable) {
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
