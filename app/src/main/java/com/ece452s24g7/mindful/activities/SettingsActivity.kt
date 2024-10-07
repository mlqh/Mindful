package com.ece452s24g7.mindful.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.ece452s24g7.mindful.R
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ece452s24g7.mindful.notifications.utils.BackupReminderScheduler.scheduleBackupReminder
import com.ece452s24g7.mindful.notifications.utils.BackupReminderScheduler.NotificationFrequency
import com.ece452s24g7.mindful.notifications.utils.JournalReminderScheduler.scheduleJournalingReminder
import com.ece452s24g7.mindful.notifications.workers.LocationWorker
import java.util.concurrent.TimeUnit


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        // Preference 'objects' to manipulate, global since they're used outside of onCreate
        private lateinit var notificationLocationPreference: SwitchPreferenceCompat
        private lateinit var notificationTimePreference: SwitchPreferenceCompat
        private lateinit var notificationBackupPreference: ListPreference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Hide AI preferences when AI is disabled
            val aiPreference = preferenceManager.findPreference<ListPreference>("ai")
            val isAiEnabled = aiPreference?.value == "ai_yes"
            val aiSummaryPreference =
                preferenceManager.findPreference<SwitchPreferenceCompat>("ai_summary")
            val aiInsightPreference =
                preferenceManager.findPreference<SwitchPreferenceCompat>("ai_insight")

            // Initial state (hide/show) is based on preference value
            aiSummaryPreference?.isVisible = isAiEnabled
            aiInsightPreference?.isVisible = isAiEnabled

            // Listener updates state if preference is changed
            aiPreference?.setOnPreferenceChangeListener { _, newValue ->
                val useAi = newValue == "ai_yes"
                aiSummaryPreference?.isVisible = useAi
                aiInsightPreference?.isVisible = useAi

                // Uncheck AI preferences when AI is disabled
                if (!useAi) {
                    aiSummaryPreference?.isChecked = false
                    aiInsightPreference?.isChecked = false
                }

                true
            }

            notificationLocationPreference = findPreference("notification_location")!!
            notificationTimePreference = findPreference("notification_time")!!
            notificationBackupPreference = findPreference("notification_backup")!!

            notificationLocationPreference.setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        notificationLocationPreference.isChecked = false

                        Toast.makeText(requireContext(), "Please grant \"Allow all the time\" location permission in system settings.", Toast.LENGTH_LONG).show()
                        return@setOnPreferenceChangeListener false
                    }
                    handleNotificationPermission()
                } else {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork("LocationWork")
                }
                true
            }

            notificationTimePreference.setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    handleNotificationPermission()
                    scheduleJournalingReminder(requireContext(), 18, 0)
                } else {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork("TimeWork")
                }
                true
            }

            notificationBackupPreference.setOnPreferenceChangeListener { _, newValue ->
                if ((newValue as String) != "NONE") {
                    handleNotificationPermission()
                    val backupFrequency: NotificationFrequency = NotificationFrequency.valueOf(newValue)
                    scheduleBackupReminder(requireContext(), backupFrequency)
                } else {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork("BackupWork")
                }
                true
            }

            // Hide set custom PIN preference when not using custom PIN lock
            val pinTypePreference = preferenceManager.findPreference<ListPreference>("lock_type")
            val isCustomPinEnabled = pinTypePreference?.value == "pin_custom"
            val setCustomPinPreference =
                preferenceManager.findPreference<EditTextPreference>("pin_lock")

            // Initial state (hide/show) is based on preference value
            setCustomPinPreference?.isVisible = isCustomPinEnabled

            // Listener updates state if preference is changed
            pinTypePreference?.setOnPreferenceChangeListener { _: Preference, newValue ->
                val useCustomPin = newValue == "pin_custom"
                setCustomPinPreference?.isVisible = useCustomPin

                if (useCustomPin) {
                    setCustomPinPreference?.text = ""
                    lifecycleScope.launch {
                        delay(100)
                        scrollToPreference("pin_lock")
                    }
                }
                true
            }

            // Text input filters for custom PIN
            setCustomPinPreference?.setOnBindEditTextListener { editText: EditText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.filters += InputFilter.LengthFilter(4)
                editText.addTextChangedListener(PinTextWatcher(editText))
            }
            setCustomPinPreference?.setOnPreferenceChangeListener { _, newValue ->
                val s = newValue as String
                s.all { char -> char.isDigit() } && s.length == 4
            }
        }

        private fun handleNotificationPermission() {
            if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        private val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                notificationLocationPreference.isChecked = false
                notificationTimePreference.isChecked = false
                notificationBackupPreference.value = "NONE"
            }
        }

        private fun startLocationWork() {
            val workRequest = PeriodicWorkRequestBuilder<LocationWorker>(12, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "LocationWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        companion object {
            private const val REQUEST_CODE_BACKGROUND_LOCATION = 1
        }
    }

    class PinTextWatcher(private val editText: EditText) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            return
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            return
        }

        override fun afterTextChanged(s: Editable?) {
            val pin = s.toString()
            if (pin.length < 4) {
                editText.error = "Minimum pin length is 4"
            } else {
                editText.error = null
            }
        }
    }
}
