package com.ece452s24g7.mindful.activities

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ece452s24g7.mindful.R
import com.ece452s24g7.mindful.backup.DatabaseBackup
import java.io.File

class RestoreFromBackupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restore_from_backup)

        val backupDir =
            File(Environment.getExternalStorageDirectory(), "Documents/MindfulBackups")

        val entries = backupDir.listFiles()?.map { file -> file.name }?.toTypedArray()
        val radioGroup = findViewById<RadioGroup>(R.id.backup_radio_group)

        for (entry in entries!!) {
            Log.d("DEBUG", "Found backup file: $entry")
            val radioButton = RadioButton(this)
            radioButton.text = entry
            radioButton.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            radioGroup.addView(radioButton)
        }

        val restoreButton = findViewById<Button>(R.id.restore_confirm)
        val keyEditText = findViewById<EditText>(R.id.decrypt_key)
        val dbBackup = DatabaseBackup(this)
        restoreButton?.setOnClickListener {
            val selectedButton = radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)

            if (selectedButton == null) {
                AlertDialog.Builder(this).setMessage("Select a backup file!").create().show()
                return@setOnClickListener
            }
            val backupFileName = selectedButton.text.toString()

            val key = keyEditText.text.toString()
            if (key.isBlank()) {
                AlertDialog.Builder(this).setMessage("Enter a decryption key!").create().show()
                return@setOnClickListener
            }

            val res = dbBackup.restoreFromBackup(backupFileName, keyEditText.text.toString())
            if (!res) {
                AlertDialog.Builder(this).setMessage("Invalid Key!").create().show()
                return@setOnClickListener
            }

            finish()
        }
    }
}