package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ReminderNotificationScheduler.ACTION_SHOW_REMINDER) return
        val reminderId = intent.getStringExtra(ReminderNotificationScheduler.EXTRA_REMINDER_ID)
            ?: "reminder_alert"
        val title = intent.getStringExtra(ReminderNotificationScheduler.EXTRA_REMINDER_TITLE)
            ?.takeIf { it.isNotBlank() } ?: return
        val notes = intent.getStringExtra(ReminderNotificationScheduler.EXTRA_REMINDER_NOTES).orEmpty()

        ReminderNotificationScheduler.postReminderNotification(
            context = context,
            reminderId = reminderId,
            title = title,
            notes = notes
        )
    }
}
