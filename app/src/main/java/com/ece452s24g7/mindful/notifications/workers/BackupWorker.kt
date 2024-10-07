package com.ece452s24g7.mindful.notifications.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class BackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Mindful Backup Reminder")
            .setContentText("Reminder to back up your journal entries! Go to Settings > Backup > Create backup.")
            .build()

        notificationManager.notify(3, notification)
    }

    companion object {
        const val CHANNEL_ID = "BackupServiceChannel"
    }
}
