package com.ece452s24g7.mindful.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ece452s24g7.mindful.R
import com.ece452s24g7.mindful.activities.viewmodels.MainViewModel
import com.ece452s24g7.mindful.adapters.EntryListAdapter
import com.ece452s24g7.mindful.notifications.workers.BackupWorker
import com.ece452s24g7.mindful.notifications.workers.LocationWorker
import com.ece452s24g7.mindful.notifications.workers.TimeWorker
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val entryListView = findViewById<RecyclerView>(R.id.entry_list)
        entryListView.layoutManager = LinearLayoutManager(this)

        viewModel.entries.observe(this) { newEntries ->
            entryListView.adapter = EntryListAdapter(newEntries, this) {
                //do nothing
            }
        }

        val mainCreateButton = findViewById<FloatingActionButton>(R.id.mainCreateButton)
        mainCreateButton.setOnClickListener {
            val createEntryIntent = Intent(this, CreateEntryActivity::class.java)
            startActivity(createEntryIntent)
        }

        val mainCalendarButton = findViewById<FloatingActionButton>(R.id.mainCalendarButton)
        mainCalendarButton.setOnClickListener {
            val calendarIntent = Intent(this, CalendarActivity::class.java)
            startActivity(calendarIntent)
        }

        val mainSettingsButton = findViewById<ImageButton>(R.id.mainSettingsButton)
        mainSettingsButton.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }

        createNotificationChannels()
    }

    // monkey way of ensuring we refresh our reference to the DB when we restore it
    override fun onResume() {
        super.onResume()
        viewModel.refreshDatabase()

        val entryListView = findViewById<RecyclerView>(R.id.entry_list)
        entryListView.layoutManager = LinearLayoutManager(this)

        viewModel.entries.observe(this) { newEntries ->
            entryListView.adapter = EntryListAdapter(newEntries, this) {
                // do nothing on callback
            }
        }
    }

    // Our manifest only targets newer Android versions, no need for a check here
    private fun createNotificationChannels() {
        val locationServiceChannel = NotificationChannel(
            LocationWorker.CHANNEL_ID,
            "Location-based Journaling Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val timeServiceChannel = NotificationChannel(
            TimeWorker.CHANNEL_ID,
            "Time-based Journaling Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val backupServiceChannel = NotificationChannel(
            BackupWorker.CHANNEL_ID,
            "Backup Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(locationServiceChannel)
        manager.createNotificationChannel(timeServiceChannel)
        manager.createNotificationChannel(backupServiceChannel)
    }
}