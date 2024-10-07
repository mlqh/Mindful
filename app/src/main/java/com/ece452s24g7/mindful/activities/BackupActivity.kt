package com.ece452s24g7.mindful.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ece452s24g7.mindful.R
import com.ece452s24g7.mindful.backup.DatabaseBackup


class BackupActivity : AppCompatActivity() {
    private var dbBackup: DatabaseBackup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_backup)

        dbBackup = DatabaseBackup(this)
        val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val keyTextView = findViewById<TextView>(R.id.backup_key)
        val fileNameEditText = findViewById<EditText>(R.id.backup_file_name)
        val createBackupButton = findViewById<Button>(R.id.confirm_backup)
        val copyButton = findViewById<ImageButton>(R.id.key_copy)

        val key = dbBackup!!.generateKey()
        keyTextView.text = key

        copyButton.setOnClickListener {
            val clip = ClipData.newPlainText("key", key);
            clipboard.setPrimaryClip(clip);
            AlertDialog.Builder(this).setMessage("Key copied to clipboard!").create().show()
        }

        createBackupButton.setOnClickListener {
            val backupName = fileNameEditText.text.toString()
            if (backupName.isBlank()) {
                AlertDialog.Builder(this).setMessage("Enter a backup name!").create().show()
            } else {
                dbBackup!!.createBackup(fileNameEditText.text.toString(), key)
                finish()
            }
        }
    }
}