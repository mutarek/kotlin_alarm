package com.dhakadigital.kotlin_code_test.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dhakadigital.kotlin_code_test.service.AlarmService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore all saved alarms after boot
            AlarmService.restoreAlarms(context)
        }
    }
}