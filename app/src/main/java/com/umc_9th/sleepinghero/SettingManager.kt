package com.umc_9th.sleepinghero

import android.content.Context

class SettingManager(context: Context) {
    private val preferences = context.getSharedPreferences("setting_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PUSH_ALARM = "setting_push_alarm"
        private const val NO_DISTURB = "setting_no_disturb"
        private const val SLEEP_TIME = "setting_sleep_time"
        private const val AWAKE_TIME = "setting_awake_time"
        private const val ALARM_TYPE = "setting_alarm_type"
        private const val ALARM_VOLUME = "setting_alarm_volume"
    }

    // PUSH_ALARM
    fun getPushAlarm(): Boolean =
        preferences.getBoolean(PUSH_ALARM, false)

    fun setPushAlarm(value: Boolean) {
        preferences.edit()
            .putBoolean(PUSH_ALARM, value)
            .apply()
    }

    // NO_DISTURB
    fun getNoDisturb(): Boolean =
        preferences.getBoolean(NO_DISTURB, false)

    fun setNoDisturb(value: Boolean) {
        preferences.edit()
            .putBoolean(NO_DISTURB, value)
            .apply()
    }

    // SLEEP_TIME
    fun getSleepTime(): String =
        preferences.getString(SLEEP_TIME, "null").toString()

    fun setSleepTime(value: String) {
        preferences.edit()
            .putString(SLEEP_TIME, value)
            .apply()
    }

    // AWAKE_TIME
    fun getAwakeTime(): String =
        preferences.getString(AWAKE_TIME, "null").toString()

    fun setAwakeTime(value: String) {
        preferences.edit()
            .putString(AWAKE_TIME, value)
            .apply()
    }

    // ALARM_TYPE
    fun getAlarmType(): Int =
        preferences.getInt(ALARM_TYPE, 0)

    fun setAlarmType(value: Int) {
        preferences.edit()
            .putInt(ALARM_TYPE, value)
            .apply()
    }

    // ALARM_VOLUME
    fun getAlarmVolume(): Int =
        preferences.getInt(ALARM_VOLUME, 0)

    fun setAlarmVolume(value: Int) {
        preferences.edit()
            .putInt(ALARM_VOLUME, value)
            .apply()
    }
}