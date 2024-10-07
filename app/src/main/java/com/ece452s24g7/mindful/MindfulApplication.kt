package com.ece452s24g7.mindful

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class MindfulApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
