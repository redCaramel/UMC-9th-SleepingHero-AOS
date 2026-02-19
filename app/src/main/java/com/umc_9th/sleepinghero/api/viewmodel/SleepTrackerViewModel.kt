package com.umc_9th.sleepinghero.api.viewmodel

import androidx.lifecycle.ViewModel

/**
 * 수면 트래커 화면 전환(Locker 등) 후 복귀해도 타이머가 계속 흐르도록 상태 보존.
 * Activity scope로 사용 (activityViewModels).
 */
class SleepTrackerViewModel : ViewModel() {

    var startMillis: Long = 0L
        private set
    var trackingStarted: Boolean = false
        private set
    var currentRecordId: Int = 0
        private set
    var alarmShown: Boolean = false
        set(value) { field = value }
    var sleepTimeStr: String = "11:00 PM"
        private set
    var awakeTimeStr: String = "07:00 AM"
        private set
    var goalMinutes: Int = 1
        private set

    fun startTracking(recordId: Int, sleepTime: String, awakeTime: String, goalMin: Int) {
        startMillis = System.currentTimeMillis()
        trackingStarted = true
        currentRecordId = recordId
        alarmShown = false
        sleepTimeStr = sleepTime
        awakeTimeStr = awakeTime
        goalMinutes = goalMin
    }

    fun markAlarmShown() {
        alarmShown = true
    }

    fun stopTracking() {
        trackingStarted = false
    }

    fun clear() {
        trackingStarted = false
        startMillis = 0L
        currentRecordId = 0
    }
}
