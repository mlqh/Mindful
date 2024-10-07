package com.ece452s24g7.mindful.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

import com.ece452s24g7.mindful.notifications.workers.TimeWorker

class JournalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val workRequest = OneTimeWorkRequestBuilder<TimeWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
