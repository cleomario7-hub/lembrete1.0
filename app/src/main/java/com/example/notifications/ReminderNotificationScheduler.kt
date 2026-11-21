package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.Reminder

class ReminderNotificationScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    init {
        ensureNotificationChannel(appContext)
    }

    fun syncAlarms(reminders: List<Reminder>) {
        val now = System.currentTimeMillis()
        reminders.forEach { reminder ->
            if (reminder.id.isNotBlank()) {
                if (!reminder.completed && reminder.notifyAlert && reminder.dueAtMillis > now) {
                    scheduleReminderAlarm(
                        reminderId = reminder.id,
                        title = reminder.title,
                        notes = reminder.notes,
                        dueAtMillis = reminder.dueAtMillis
                    )
                } else {
                    cancelReminderAlarm(reminder.id)
                }
            }
        }
    }

    fun scheduleReminderAlarm(
        reminderId: String,
        title: String,
        notes: String,
        dueAtMillis: Long
    ) {
        val manager = alarmManager ?: return
        if (dueAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(appContext, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
            putExtra(EXTRA_REMINDER_NOTES, notes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                manager.canScheduleExactAlarms()
            if (canExact) {
                // Alarme no minuto certo, mesmo com o celular em repouso (Doze).
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dueAtMillis,
                    pendingIntent
                )
            } else {
                manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dueAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Failed to schedule alarm: ${e.message}")
        }
    }

    fun cancelReminderAlarm(reminderId: String) {
        val manager = alarmManager ?: return
        val intent = Intent(appContext, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            manager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        private const val TAG = "ReminderScheduler"
        const val CHANNEL_ID = "lembretes_alerts_channel"
        const val ACTION_SHOW_REMINDER = "com.example.notifications.SHOW_REMINDER"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_REMINDER_NOTES = "extra_reminder_notes"

        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notif_channel_desc)
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }

        fun postReminderNotification(
            context: Context,
            reminderId: String,
            title: String,
            notes: String
        ) {
            ensureNotificationChannel(context)
            if (!hasNotificationPermission(context)) return

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                reminderId.hashCode(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val bodyText = notes.ifBlank { context.getString(R.string.notif_default_body) }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(bodyText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context)
                    .notify(reminderId.hashCode(), notification)
            } catch (e: SecurityException) {
                Log.w(TAG, "Notification permission missing: ${e.message}")
            }
        }
    }
}
