package com.ece452s24g7.mindful.notifications.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ece452s24g7.mindful.notifications.receivers.BackupReminderReceiver
import java.util.Calendar

object BackupReminderScheduler {
    fun scheduleBackupReminder(context: Context, frequency: NotificationFrequency) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, BackupReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intervalMillis = when (frequency) {
            NotificationFrequency.WEEKLY -> AlarmManager.INTERVAL_DAY * 7
            NotificationFrequency.BIWEEKLY -> AlarmManager.INTERVAL_DAY * 14
            NotificationFrequency.MONTHLY -> AlarmManager.INTERVAL_DAY * 30L
            NotificationFrequency.QUARTERLY -> AlarmManager.INTERVAL_DAY * 91L
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            intervalMillis,
            pendingIntent
        )
    }

    enum class NotificationFrequency {
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        QUARTERLY
    }
}
