package com.umc_9th.sleepinghero

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.repository.HomeRepository
import com.umc_9th.sleepinghero.databinding.ActivityTimeSettingBinding
import com.umc_9th.sleepinghero.databinding.FragmentRoutineBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class RoutineFragment : Fragment() {

    private lateinit var binding: FragmentRoutineBinding
    private lateinit var mainActivity: MainActivity

    private val homeRepository by lazy { HomeRepository() }

    private enum class ReportMode { WEEKLY, MONTHLY }
    private var currentMode: ReportMode = ReportMode.WEEKLY // 기본값 주간

    private val dateFmtWeekly = DateTimeFormatter.ofPattern("M/d", Locale.KOREA)
    private val dateFmtMonthly = DateTimeFormatter.ofPattern("M/d", Locale.KOREA)

    // ✅ 더미 데이터(현재 화면 표시용으로 보관)
    private var weeklyLabels: List<String> = emptyList()
    private var weeklyHours: List<Float> = emptyList()

    private var monthlyLabels: List<String> = emptyList()
    private var monthlyHours: List<Float> = emptyList()

    // 기록일 판정 기준 (너가 원하는대로 바꾸면 됨)
    // 예: 0f이면 미기록, 1f 이상이면 기록으로 취급
    private val recordedThresholdHours = 1f

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

        updateGoalSleep()
        fetchStreak()

        binding.bedContainer.setOnClickListener {
            showCustomTimeDialog(
                title = "취침 시간 설정",
                targetTextView = binding.tvBedTime
            )
        }

        binding.wakeContainer.setOnClickListener {
            showCustomTimeDialog(
                title = "기상 시간 설정",
                targetTextView = binding.tvWakeTime
            )
        }

        // ✅ 차트 초기 설정
        initWeeklyBarChart(binding.barChartWeekly)
        initMonthlyLineChart(binding.lineChartMonthly)

        // ✅ 더미 데이터 생성 + 차트/레포트 렌더
        renderWeeklyDummy()
        renderMonthlyDummy()

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

    private fun fetchStreak() {
        viewLifecycleOwner.lifecycleScope.launch {

            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) {
                Log.e("ROUTINE_TOKEN", "Token is null")
                return@launch
            }

            val token = "Bearer $raw"
            val res = homeRepository.getDashboard(token)

            if (res.isSuccess && res.result != null) {

                val streak = res.result.currentStreak
                binding.tvStreakDays.text = "${streak}일"

                binding.tvBonus.text = when {
                    streak <= 0 -> "없음"
                    streak in 1..2 -> "+1% EXP"
                    streak in 3..6 -> "+3% EXP"
                    else -> "+5% EXP"
                }

            } else {
                Log.e("ROUTINE_ERROR", res.message)
            }
        }
    }

    private fun applyReportMode(mode: ReportMode) {
        val selectedBg = R.drawable.bg_toggle_selected
        val unselectedBg = R.drawable.bg_toggle_unselected

        when (mode) {
            ReportMode.WEEKLY -> {
                binding.btnWeekly.setBackgroundResource(selectedBg)
                binding.btnWeekly.setTextColor(Color.WHITE)

                binding.btnMonthly.setBackgroundResource(unselectedBg)
                binding.btnMonthly.setTextColor(Color.parseColor("#404040"))

                binding.tvMonthlyTitle.text = "주간 리포트"

                binding.barChartWeekly.visibility = View.VISIBLE
                binding.lineChartMonthly.visibility = View.GONE
                binding.barChartWeekly.invalidate()

                // ✅ 레포트(총시간/일평균/기록일) 갱신
                updateReportFromHours(
                    mode = ReportMode.WEEKLY,
                    hours = weeklyHours
                )
            }

            ReportMode.MONTHLY -> {
                binding.btnMonthly.setBackgroundResource(selectedBg)
                binding.btnMonthly.setTextColor(Color.WHITE)

                binding.btnWeekly.setBackgroundResource(unselectedBg)
                binding.btnWeekly.setTextColor(Color.parseColor("#404040"))

                binding.tvMonthlyTitle.text = "월간 리포트"

                binding.barChartWeekly.visibility = View.GONE
                binding.lineChartMonthly.visibility = View.VISIBLE
                binding.lineChartMonthly.invalidate()

                // ✅ 레포트(총시간/일평균/기록일) 갱신
                updateReportFromHours(
                    mode = ReportMode.MONTHLY,
                    hours = monthlyHours
                )
            }
        }
    }

    // -----------------------------
    // 1) 주간 막대 차트
    // -----------------------------
    private fun initWeeklyBarChart(chart: BarChart) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)

        chart.axisRight.isEnabled = false

        val left = chart.axisLeft
        left.axisMinimum = 0f
        left.axisMaximum = 10f
        left.granularity = 1f
        left.setDrawGridLines(true)
        left.textColor = Color.parseColor("#6B7280")

        val x = chart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.setDrawGridLines(false)
        x.granularity = 1f
        x.textColor = Color.parseColor("#6B7280")
    }

    private fun renderWeeklyDummy() {
        val today = LocalDate.now()

        weeklyLabels = (6 downTo 0).map { d ->
            today.minusDays(d.toLong()).format(dateFmtWeekly)
        }

        // 더미 수면시간(시간)
        weeklyHours = listOf(6.7f, 8.9f, 7.0f, 5.4f, 7.8f, 6.5f, 5.8f)

        val entries = weeklyHours.mapIndexed { i, h -> BarEntry(i.toFloat(), h) }

        val dataSet = BarDataSet(entries, "")
        dataSet.setDrawValues(false)

        // Bar마다 색 다르게
        val colors = weeklyHours.map { h ->
            when {
                h < 6f -> Color.parseColor("#E74C3C")   // red
                h < 7f -> Color.parseColor("#2E86DE")   // blue
                else -> Color.parseColor("#27AE60")     // green
            }
        }
        dataSet.colors = colors

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        binding.barChartWeekly.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val idx = value.roundToInt()
                return weeklyLabels.getOrNull(idx) ?: ""
            }
        }

        binding.barChartWeekly.data = data
        binding.barChartWeekly.invalidate()
    }

    // -----------------------------
    // 2) 월간 꺾은선 차트 (이번 달 1일~말일)
    // -----------------------------
    private fun initMonthlyLineChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)

        chart.axisRight.isEnabled = false

        val left = chart.axisLeft
        left.axisMinimum = 0f
        left.axisMaximum = 10f
        left.granularity = 1f
        left.setDrawGridLines(true)
        left.textColor = Color.parseColor("#6B7280")

        val x = chart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.setDrawGridLines(false)
        x.granularity = 1f
        x.textColor = Color.parseColor("#6B7280")
    }

    private fun renderMonthlyDummy() {
        val today = LocalDate.now()
        val ym = YearMonth.from(today)
        val daysInMonth = ym.lengthOfMonth()

        val firstDay = ym.atDay(1)

        monthlyLabels = (0 until daysInMonth).map { i ->
            firstDay.plusDays(i.toLong()).format(dateFmtMonthly)
        }

        // 더미: 이번 달 전체 일수만큼 만들기
        // 스샷 느낌처럼 초반은 거의 0에 가깝다가 중간부터 값 생기게
        monthlyHours = MutableList(daysInMonth) { 0.0f }.apply {
            val startIdx = (daysInMonth * 0.55f).toInt().coerceIn(0, daysInMonth - 1)
            for (i in startIdx until daysInMonth) {
                val sample = listOf(7.8f, 9.1f, 7.2f, 8.7f, 6.9f, 9.6f, 8.1f, 5.7f, 8.4f, 7.9f)
                this[i] = sample[(i - startIdx) % sample.size]
            }
        }

        val entries = monthlyHours.mapIndexed { i, h -> Entry(i.toFloat(), h) }

        val dataSet = LineDataSet(entries, "")
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 3f
        dataSet.lineWidth = 2f
        dataSet.mode = LineDataSet.Mode.LINEAR

        dataSet.color = Color.parseColor("#2E86DE")
        dataSet.setCircleColor(Color.parseColor("#2E86DE"))

        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#A9C8FF")

        val data = LineData(dataSet)

        binding.lineChartMonthly.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val idx = value.roundToInt()
                if (idx !in 0 until monthlyLabels.size) return ""

                // 5일 간격 + 마지막날
                return if (idx % 5 == 0 || idx == monthlyLabels.lastIndex) monthlyLabels[idx] else ""
            }
        }

        binding.lineChartMonthly.data = data
        binding.lineChartMonthly.invalidate()
    }

    // -----------------------------
    // ✅ 레포트 계산 로직 (총시간 / 일평균 / 기록일)
    // -----------------------------
    private fun updateReportFromHours(mode: ReportMode, hours: List<Float>) {
        if (hours.isEmpty()) {
            binding.tvTotalHours.text = "0h"
            binding.tvDailyAvg.text = "0.0h"
            binding.tvRecordedDays.text = when (mode) {
                ReportMode.WEEKLY -> "0/7"
                ReportMode.MONTHLY -> "0/30"
            }
            return
        }

        val total = hours.sum().coerceAtLeast(0f)
        val avg = total / hours.size

        // 기록일: threshold 이상인 날만 카운트 (0값/극소값은 미기록 처리)
        val recorded = hours.count { it >= recordedThresholdHours }
        val denom = hours.size

        binding.tvTotalHours.text = "${total.toInt()}h"          // 예: 52h
        binding.tvDailyAvg.text = String.format(Locale.KOREA, "%.1fh", avg) // 예: 7.4h
        binding.tvRecordedDays.text = "${recorded}/${denom}"
    }

    // -----------------------------
    // 이하: 기존 시간 설정 로직 그대로
    // -----------------------------
    private fun showCustomTimeDialog(title: String, targetTextView: TextView) {
        val dialogBinding = ActivityTimeSettingBinding.inflate(layoutInflater)

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
            val finalStr =
                "${makeTimeString(hour, 0)}:${makeTimeString(min, 0)} ${makeTimeString(ampm, 1)}"
            targetTextView.text = finalStr
            updateGoalSleep()
            dialog.dismiss()
        }

        dialogBinding.btnTimesetCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateGoalSleep() {
        val bedStr = binding.tvBedTime.text.toString()
        val wakeStr = binding.tvWakeTime.text.toString()

        val bedMin = timeStringToMinutesSafe(bedStr)
        val wakeMin = timeStringToMinutesSafe(wakeStr)

        if (bedMin == null || wakeMin == null) return

        var diff = wakeMin - bedMin
        if (diff < 0) diff += 24 * 60

        binding.tvGoalValue.text = minutesToKoreanHourMin(diff)
    }

    private fun minutesToKoreanHourMin(totalMin: Int): String {
        val h = totalMin / 60
        val m = totalMin % 60

        return if (m == 0) "${h}시간" else "${h}시간 ${m}분"
    }

    private fun timeStringToMinutesSafe(timeStr: String): Int? {
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

        var hour24 = hour12 % 12
        if (isPm) hour24 += 12

        return hour24 * 60 + minute
    }

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
