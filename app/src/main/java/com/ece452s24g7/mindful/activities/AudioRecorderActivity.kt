package com.ece452s24g7.mindful.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ece452s24g7.mindful.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Runnable
import java.util.Locale
import java.util.UUID

class AudioRecorderActivity : AppCompatActivity() {
    private lateinit var recordButton: FloatingActionButton
    private lateinit var exitButton: Button
    private lateinit var timerView: TextView
    private lateinit var timerHandler: Handler
    private lateinit var timerRunnable: Runnable

    private var elapsedTime = -1
    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var savedFilePath: String? = null
    private var currFilePath: String? = null

    private val audioRequestCode = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_recorder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timerView = findViewById(R.id.audio_recorder_timer)
        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = Runnable {
            elapsedTime++
            timerView.text = formatTime(elapsedTime)
            timerHandler.postDelayed(timerRunnable, 1000)
        }

        exitButton = findViewById(R.id.audio_recorder_exit_button)
        exitButton.setOnClickListener {
            setResult(RESULT_OK, Intent().putExtra("entry_audioPath", savedFilePath))
            finish()
        }

        recordButton = findViewById(R.id.audio_recorder_record_button)
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else if (audioPermGranted()) {
                startRecording()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        }
    }

    private fun startRecording() {
        savedFilePath = null
        timerView.text = "00:00"
        elapsedTime = -1
        timerHandler.post(timerRunnable)
        recordButton.setImageResource(R.drawable.baseline_stop_24)
        isRecording = true
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(16 * 44100)
            currFilePath = filesDir.absolutePath + "/" + UUID.randomUUID().toString() + ".3gp"
            setOutputFile(currFilePath)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        savedFilePath = currFilePath
        timerHandler.removeCallbacks(timerRunnable)
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        recordButton.setImageResource(R.drawable.baseline_mic_24)
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }

    private fun audioPermGranted(): Boolean {
        val perm = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), audioRequestCode)
            return false
        }
        return true
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.CANADA, "%02d:%02d", mins, secs)
    }
}