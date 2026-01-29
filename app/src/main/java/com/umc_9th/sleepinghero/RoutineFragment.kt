package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.umc_9th.sleepinghero.databinding.ActivityTimeSettingBinding
import com.umc_9th.sleepinghero.databinding.FragmentRoutineBinding
import java.util.Locale

class RoutineFragment : Fragment() {

    private lateinit var binding: FragmentRoutineBinding
    private lateinit var mainActivity: MainActivity
    private enum class ReportMode { WEEKLY, MONTHLY }
    private var currentMode: ReportMode = ReportMode.WEEKLY // 기본값 주간

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 화면 처음 들어왔을 때도 계산 한번
        updateGoalSleep()

        // 취침 시간
        binding.bedContainer.setOnClickListener {
            showCustomTimeDialog(
                title = "취침 시간 설정",
                targetTextView = binding.tvBedTime
            )
        }

        // 기상 시간
        binding.wakeContainer.setOnClickListener {
            showCustomTimeDialog(
                title = "기상 시간 설정",
                targetTextView = binding.tvWakeTime
            )
        }

        // ✅ 토글 초기화 + 클릭 리스너
        applyReportMode(currentMode)

        binding.btnWeekly.setOnClickListener {
            currentMode = ReportMode.WEEKLY
            applyReportMode(currentMode)
        }

        binding.btnMonthly.setOnClickListener {
            currentMode = ReportMode.MONTHLY
            applyReportMode(currentMode)
        }

    }

    private fun applyReportMode(mode: ReportMode) {
        val selectedBg = R.drawable.bg_toggle_selected
        val unselectedBg = R.drawable.bg_toggle_unselected

        when (mode) {
            ReportMode.WEEKLY -> {
                binding.btnWeekly.setBackgroundResource(selectedBg)
                binding.btnWeekly.setTextColor(android.graphics.Color.WHITE)

                binding.btnMonthly.setBackgroundResource(unselectedBg)
                binding.btnMonthly.setTextColor(android.graphics.Color.parseColor("#404040"))

                // TODO - 텍스트만 바꾸기 (데이터는 나중에)
                binding.tvMonthlyTitle.text = "주간 리포트"
            }

            ReportMode.MONTHLY -> {
                binding.btnMonthly.setBackgroundResource(selectedBg)
                binding.btnMonthly.setTextColor(android.graphics.Color.WHITE)

                binding.btnWeekly.setBackgroundResource(unselectedBg)
                binding.btnWeekly.setTextColor(android.graphics.Color.parseColor("#404040"))

                binding.tvMonthlyTitle.text = "월간 리포트"
            }
        }
    }

    private fun showCustomTimeDialog(title: String, targetTextView: TextView) {
        val dialogBinding = ActivityTimeSettingBinding.inflate(layoutInflater)

        // 현재 텍스트뷰에 적힌 시간("11:00 PM")을 파싱해서 초기값으로 세팅
        val time = parseTimeString(targetTextView.text.toString())
        var hour = time.first
        var min = time.second
        var ampm = time.third

        dialogBinding.tvTimesetHour.text = makeTimeString(hour, 0)
        dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
        dialogBinding.tvTimesetAmpm.text = makeTimeString(ampm, 1)

        val dialog = AlertDialog.Builder(mainActivity, R.style.PopupAnimStyle)
            .setView(dialogBinding.root)
            .setTitle(title)
            .create()

        // ===== 팀원 코드 그대로(로직 변경 없음) =====
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
            targetTextView.text = finalStr

            // ✅ 여기서 목표 수면시간 갱신
            updateGoalSleep()

            dialog.dismiss()
        }

        dialogBinding.btnTimesetCancel.setOnClickListener {
            dialog.dismiss()
        }
        // =======================================

        dialog.show()
    }

    private fun updateGoalSleep() {
        val bedStr = binding.tvBedTime.text.toString()
        val wakeStr = binding.tvWakeTime.text.toString()

        val bedMin = timeStringToMinutesSafe(bedStr)
        val wakeMin = timeStringToMinutesSafe(wakeStr)

        // 파싱 실패하면 걍 업데이트 안 함 (크래시 방지)
        if (bedMin == null || wakeMin == null) return

        var diff = wakeMin - bedMin
        if (diff < 0) diff += 24 * 60  // 다음날 기상 처리

        binding.tvGoalValue.text = minutesToKoreanHourMin(diff)
    }

    private fun minutesToKoreanHourMin(totalMin: Int): String {
        val h = totalMin / 60
        val m = totalMin % 60

        return if (m == 0) {
            "${h}시간"
        } else {
            "${h}시간 ${m}분"
        }
    }

    // "11:00 PM" -> 분(0~1439)
    private fun timeStringToMinutesSafe(timeStr: String): Int? {
        // 기대 포맷: "hh:mm AM" 또는 "hh:mm PM"
        val parts = timeStr.trim().split(" ")
        if (parts.size < 2) return null

        val hm = parts[0]
        val ampmStr = parts[1].uppercase(Locale.getDefault())

        val hmParts = hm.split(":")
        if (hmParts.size != 2) return null

        val hour12 = hmParts[0].toIntOrNull() ?: return null
        val minute = hmParts[1].toIntOrNull() ?: return null
        if (hour12 !in 1..12) return null
        if (minute !in 0..59) return null

        val isPm = (ampmStr == "PM")
        val isAm = (ampmStr == "AM")
        if (!isPm && !isAm) return null

        // 12시간 -> 24시간
        // 12 AM = 0시, 12 PM = 12시
        var hour24 = hour12 % 12
        if (isPm) hour24 += 12

        return hour24 * 60 + minute
    }


    // ===== 팀원 코드 그대로 활용 =====
    private fun parseTimeString(timeStr: String): Triple<Int, Int, Int> {
        val parts = timeStr.split(" ")
        val time = parts[0]
        val ampm = parts[1]
        val (hourStr, minuteStr) = time.split(":")
        val hour = hourStr.toInt()
        val minute = minuteStr.toInt()
        val ampmFlag = if (ampm.equals("PM", ignoreCase = true)) 1 else 0
        return Triple(hour, minute, ampmFlag)
    }

    private fun makeTimeString(time: Int, type: Int): String {
        return if (type == 1) {
            if (time == 0) "AM" else "PM"
        } else {
            if (time < 10) "0$time" else time.toString()
        }
    }
}
