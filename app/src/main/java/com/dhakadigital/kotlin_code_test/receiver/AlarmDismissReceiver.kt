package com.dhakadigital.kotlin_code_test.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dhakadigital.kotlin_code_test.service.AlarmService


class AlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // Get alarm ID
        val alarmId = intent.getIntExtra("ALARM_ID", -1)

        // Cancel the alarm
        if (alarmId != -1) {
            AlarmService.cancelAlarm(context, alarmId)

            // Cancel notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(alarmId)

            // Stop alarm sound
//            Intent(context, AlarmSoundService::class.java).also { stopIntent ->
//                context.stopService(stopIntent)
//            }
        }
    }
}