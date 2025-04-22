package com.dhakadigital.kotlin_code_test.receiver

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dhakadigital.kotlin_code_test.AlarmActivity
import com.dhakadigital.kotlin_code_test.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return // Acquire wake lock to ensure the device wakes up

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "AlarmApp:AlarmWakeLock"
        )
        wakeLock.acquire(1 * 60 * 1000L) // 3 minutes timeout

        // Get alarm details from intent
        val title = intent.getStringExtra("ALARM_TITLE") ?: "Reminder Alarm"
        val message = intent.getStringExtra("ALARM_MESSAGE") ?: "!"
        val timeInMillis = intent.getLongExtra("ALARM_TIME", System.currentTimeMillis())
        val alarmId = intent.getIntExtra("ALARM_ID", -1)

        // Create intent for AlarmActivity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ALARM_TITLE", title)
            putExtra("ALARM_MESSAGE", message)
            putExtra("ALARM_TIME", timeInMillis)
            putExtra("ALARM_ID", alarmId)
            putExtra("FROM_ALARM", true)
        }

        showNotification(context, title  ,message , timeInMillis, alarmId, alarmIntent)

        // Optionally still try to start Activity
        try {
            context.startActivity(alarmIntent)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start activity: ${e.message}")
        }

    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        timeInMillis: Long,
        alarmId: Int,
        fullScreenIntent: Intent
    ) {
        // Create notification channel for Android 8.0+
        createNotificationChannel(context)

        // Create full screen pending intent
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create dismiss action
        val dismissIntent = Intent(context, AlarmDismissReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra("ALARM_TITLE", title)
            putExtra("ALARM_MESSAGE", message)
            putExtra("ALARM_TIME", timeInMillis)
            putExtra("ALARM_ID", alarmId)
            putExtra("FROM_ALARM", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullSScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, "Dismiss", dismissPendingIntent)
            .addAction(0, "Open", fullSScreenPendingIntent)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(alarmId, builder.build())
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for alarms"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }

    companion object {
        const val CHANNEL_ID = "alarm_notification_channel"
    }
}