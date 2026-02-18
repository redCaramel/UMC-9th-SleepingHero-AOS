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
import com.umc_9th.sleepinghero.api.ApiClient
import com.umc_9th.sleepinghero.api.TokenManager
import com.umc_9th.sleepinghero.api.dto.SleepSessionItem
import com.umc_9th.sleepinghero.api.repository.HomeRepository
import com.umc_9th.sleepinghero.api.repository.SleepRepository
import com.umc_9th.sleepinghero.databinding.ActivityTimeSettingBinding
import com.umc_9th.sleepinghero.databinding.FragmentRoutineBinding
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class RoutineFragment : Fragment() {

    private lateinit var binding: FragmentRoutineBinding
    private lateinit var mainActivity: MainActivity

    private val homeRepository by lazy { HomeRepository(ApiClient.homeService) }
    private val sleepRepository by lazy { SleepRepository(ApiClient.sleepService) }

    private enum class ReportMode { WEEKLY, MONTHLY }
    private var currentMode: ReportMode = ReportMode.WEEKLY

    private val dateFmtWeekly = DateTimeFormatter.ofPattern("M/d", Locale.KOREA)
    private val dateFmtMonthly = DateTimeFormatter.ofPattern("M/d", Locale.KOREA)

    private var weeklyLabels: List<String> = emptyList()
    private var weeklyHours: List<Float> = emptyList()

    private var monthlyLabels: List<String> = emptyList()
    private var monthlyHours: List<Float> = emptyList()

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

        initWeeklyBarChart(binding.barChartWeekly)
        initMonthlyLineChart(binding.lineChartMonthly)

        applyReportMode(currentMode)

        binding.btnWeekly.setOnClickListener {
            currentMode = ReportMode.WEEKLY
            applyReportMode(currentMode)
        }

        binding.btnMonthly.setOnClickListener {
            currentMode = ReportMode.MONTHLY
            applyReportMode(currentMode)
        }

        binding.bedContainer.setOnClickListener {
            showCustomTimeDialog("취침 시간 설정", binding.tvBedTime)
        }
        binding.wakeContainer.setOnClickListener {
            showCustomTimeDialog("기상 시간 설정", binding.tvWakeTime)
        }

        fetchSleepSessionsAndRender()
    }

    private fun fetchStreak() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) {
                Log.e("ROUTINE_TOKEN", "Token is null")
                return@launch
            }

            val result = homeRepository.getDashboard(raw)

            result.onSuccess { data ->
                val streak = data.currentStreak
                binding.tvStreakDays.text = "${streak}일"
            }.onFailure { e ->
                Log.e("ROUTINE_ERROR", e.message ?: "unknown error")
            }
        }
    }

    private fun fetchSleepSessionsAndRender() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) {
                Log.e("ROUTINE_TOKEN", "Token is null")
                seedEmptyThenRender()
                return@launch
            }


            val result = sleepRepository.getSleepSessions(raw, page = 0, size = 200)

            result.onSuccess { page ->
                val records = page.content

                buildWeeklyFromRecords(records)
                buildMonthlyFromRecords(records)

                renderWeeklyFromPrepared()
                renderMonthlyFromPrepared()

                applyReportMode(currentMode)
            }.onFailure { e ->
                Log.e("ROUTINE_SLEEP_ERR", e.message ?: "unknown error")
                seedEmptyThenRender()
            }
        }
    }


    private fun seedEmptyThenRender() {
        buildWeeklyFromRecords(emptyList())
        buildMonthlyFromRecords(emptyList())
        renderWeeklyFromPrepared()
        renderMonthlyFromPrepared()
        applyReportMode(currentMode)
    }

    private fun buildWeeklyFromRecords(records: List<SleepSessionItem>) {
        val today = LocalDate.now()
        val start = today.minusDays(6)

        val minutesByDay = mutableMapOf<LocalDate, Int>()

        for (r in records) {
            val sleptDate = parseLocalDate(r.sleptTime) ?: continue
            if (sleptDate.isBefore(start) || sleptDate.isAfter(today)) continue

            val mins = calcDurationMinutes(r.sleptTime, r.wokeTime) ?: continue
            if (mins <= 0) continue

            minutesByDay[sleptDate] = (minutesByDay[sleptDate] ?: 0) + mins
        }

        weeklyLabels = (0..6).map { i ->
            start.plusDays(i.toLong()).format(dateFmtWeekly)
        }

        weeklyHours = (0..6).map { i ->
            val d = start.plusDays(i.toLong())
            (minutesByDay[d] ?: 0) / 60f
        }
    }

    private fun buildMonthlyFromRecords(records: List<SleepSessionItem>) {
        val today = LocalDate.now()
        val ym = YearMonth.from(today)
        val daysInMonth = ym.lengthOfMonth()
        val firstDay = ym.atDay(1)
        val lastDay = ym.atEndOfMonth()

        val minutesByDay = mutableMapOf<LocalDate, Int>()

        for (r in records) {
            val sleptDate = parseLocalDate(r.sleptTime) ?: continue
            if (sleptDate.isBefore(firstDay) || sleptDate.isAfter(lastDay)) continue

            val mins = calcDurationMinutes(r.sleptTime, r.wokeTime) ?: continue
            if (mins <= 0) continue

            minutesByDay[sleptDate] = (minutesByDay[sleptDate] ?: 0) + mins
        }

        monthlyLabels = (0 until daysInMonth).map { i ->
            firstDay.plusDays(i.toLong()).format(dateFmtMonthly)
        }

        monthlyHours = (0 until daysInMonth).map { i ->
            val d = firstDay.plusDays(i.toLong())
            (minutesByDay[d] ?: 0) / 60f
        }
    }

    private fun parseLocalDate(iso: String): LocalDate? = try {
        OffsetDateTime.parse(iso).toLocalDate()
    } catch (_: Exception) {
        try { LocalDateTime.parse(iso).toLocalDate() } catch (_: Exception) { null }
    }


    private fun calcDurationMinutes(sleptIso: String, wokeIso: String): Int? {
        fun parseAny(str: String) = try {
            OffsetDateTime.parse(str).toInstant()
        } catch (_: Exception) {
            try { LocalDateTime.parse(str).atZone(ZoneId.systemDefault()).toInstant() } catch (_: Exception) { null }
        }

        val s = parseAny(sleptIso) ?: return null
        val w = parseAny(wokeIso) ?: return null
        return Duration.between(s, w).toMinutes().toInt()
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
                binding.layoutWeeklyLegend.visibility = View.VISIBLE

                updateReportFromHours(mode, weeklyHours)
            }

            ReportMode.MONTHLY -> {
                binding.btnMonthly.setBackgroundResource(selectedBg)
                binding.btnMonthly.setTextColor(Color.WHITE)

                binding.btnWeekly.setBackgroundResource(unselectedBg)
                binding.btnWeekly.setTextColor(Color.parseColor("#404040"))

                binding.tvMonthlyTitle.text = "월간 리포트"

                binding.barChartWeekly.visibility = View.GONE
                binding.lineChartMonthly.visibility = View.VISIBLE
                binding.layoutWeeklyLegend.visibility = View.GONE

                updateReportFromHours(mode, monthlyHours)
            }
        }
    }

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

    private fun renderWeeklyFromPrepared() {
        val entries = weeklyHours.mapIndexed { i, h -> BarEntry(i.toFloat(), h) }

        val dataSet = BarDataSet(entries, "")
        dataSet.setDrawValues(false)

        val colors = weeklyHours.map { h ->
            when {
                h < 6f -> Color.parseColor("#B91C14")
                h < 7f -> Color.parseColor("#1B4E81")
                else -> Color.parseColor("#2F623C")
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

    private fun renderMonthlyFromPrepared() {
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
                return if (idx % 5 == 0 || idx == monthlyLabels.lastIndex) monthlyLabels[idx] else ""
            }
        }

        binding.lineChartMonthly.data = data
        binding.lineChartMonthly.invalidate()
    }

    private fun updateReportFromHours(mode: ReportMode, hours: List<Float>) {
        val total = hours.sum().coerceAtLeast(0f)
        val recorded = hours.count { it >= recordedThresholdHours }
        val avg = if (recorded > 0) total / recorded else 0f

        binding.tvTotalHours.text = "${total.toInt()}h"
        binding.tvDailyAvg.text = String.format(Locale.KOREA, "%.1fh", avg)
        binding.tvRecordedDays.text = "${recorded}/${hours.size}"

        val isStable = recorded == hours.size
        binding.tvStatus.text = if (isStable) "안정" else "불안정"
    }

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
            min = if (min == 59) 0 else min + 1
            dialogBinding.tvTimesetMin.text = makeTimeString(min, 0)
        }

        dialogBinding.btnTimesetMindown.setOnClickListener {
            min = if (min == 0) 59 else min - 1
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

            updateGoalSleep()
            putGoalSleepToServer()

            dialog.dismiss()
        }

        dialogBinding.btnTimesetCancel.setOnClickListener { dialog.dismiss() }
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

    private fun time12hToHHmm(timeStr: String): String? {
        val minutes = timeStringToMinutesSafe(timeStr) ?: return null
        val h = minutes / 60
        val m = minutes % 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }


    private fun putGoalSleepToServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenManager.getAccessToken(requireContext())
            if (raw.isNullOrEmpty()) return@launch

            val sleepHHmmKst = time12hToHHmm(binding.tvBedTime.text.toString()) ?: return@launch
            val wakeHHmmKst  = time12hToHHmm(binding.tvWakeTime.text.toString()) ?: return@launch

            Log.d("GOAL_PUT", "kst=$sleepHHmmKst~$wakeHHmmKst")

            val result = sleepRepository.setSleepGoal(
                token = raw,
                sleepTime = sleepHHmmKst,
                wakeTime = wakeHHmmKst
            )

            result.onSuccess { data ->
                binding.tvGoalValue.text = minutesToKoreanHourMin(data.totalMinutes)
            }.onFailure { e ->
                Log.e("GOAL_PUT", e.message ?: "unknown")
            }
        }
    }


}
