package com.umc_9th.sleepinghero

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class SleepGoalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        showGoalReachedNotification(context)
    }

    companion object {
        private const val GOAL_CHANNEL_ID = "sleep_goal_channel"
        private const val GOAL_NOTI_ID = 91002

        fun showGoalReachedNotification(context: Context) {
            // Android 13+ 알림 권한 없으면 표시 불가
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            val nm = NotificationManagerCompat.from(context)
            val channel = NotificationChannelCompat.Builder(
                GOAL_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_HIGH
            )
                .setName("Sleep Goal")
                .setDescription("목표 수면시간 달성 알림")
                .build()
            nm.createNotificationChannel(channel)

            val noti = NotificationCompat.Builder(context, GOAL_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("목표 수면시간 달성")
                .setContentText("설정한 수면 시간이 종료되었습니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            nm.notify(91002, noti)
        }
    }
}
