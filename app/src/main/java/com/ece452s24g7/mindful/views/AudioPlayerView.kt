package com.ece452s24g7.mindful.views

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ece452s24g7.mindful.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Runnable
import java.util.Locale
import kotlin.math.round

class AudioPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val layout: ConstraintLayout
    private val playButton: FloatingActionButton
    private val seekBar: SeekBar
    private val timeView: TextView

    private lateinit var seekBarHandler: Handler
    private lateinit var seekBarRunnable: Runnable
    private var player: MediaPlayer? = null
    private var isPlaying = false

    init {
        LayoutInflater.from(context).inflate(R.layout.audio_player, this, true)
        layout = findViewById(R.id.audio_player_layout)
        playButton = findViewById(R.id.audio_player_play_button)
        seekBar = findViewById(R.id.audio_player_seekbar)
        timeView = findViewById(R.id.audio_player_time_view)

        playButton.setOnClickListener {
            if (isPlaying) {
                stopPlaying()
            } else {
                startPlaying()
            }
        }
    }

    fun setFilePath(path: String?) {
        if (path == null) {
            teardown()
            layout.visibility = GONE
        } else {
            teardown()
            prepare(path)
            layout.visibility = VISIBLE
        }
    }

    private fun prepare(path: String) {
        val mp = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            seekTo(0)
        }
        seekBar.max = mp.duration
        seekBar.progress = mp.currentPosition
        updateTime(mp.currentPosition, mp.duration)
        seekBarHandler = Handler(Looper.getMainLooper())
        seekBarRunnable = Runnable {
            seekBar.progress = mp.currentPosition
            updateTime(mp.currentPosition, mp.duration)
            seekBarHandler.postDelayed(seekBarRunnable, 1000)
        }
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mp.seekTo(progress)
                    updateTime(mp.currentPosition, mp.duration)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seekBarHandler.removeCallbacks(seekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isPlaying) {
                    seekBarHandler.post(seekBarRunnable)
                }
            }
        })
        mp.setOnCompletionListener {
            isPlaying = false
            playButton.setImageResource(R.drawable.baseline_play_arrow_48)
            seekBarHandler.removeCallbacks(seekBarRunnable)
            seekBar.progress = mp.currentPosition
            updateTime(mp.currentPosition, mp.duration)
            mp.seekTo(0)
        }
        player = mp
    }

    private fun teardown() {
        if (this::seekBarHandler.isInitialized) {
            seekBarHandler.removeCallbacks(seekBarRunnable)
        }
        player?.release()
        player = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        teardown()
    }

    private fun startPlaying() {
        isPlaying = true
        playButton.setImageResource(R.drawable.baseline_pause_24)
        player?.start()
        seekBarHandler.post(seekBarRunnable)
    }

    private fun stopPlaying() {
        isPlaying = false
        playButton.setImageResource(R.drawable.baseline_play_arrow_48)
        player?.pause()
        seekBarHandler.removeCallbacks(seekBarRunnable)
    }

    private fun updateTime(currentPosition: Int, duration: Int) {
        timeView.text = "${formatTime(currentPosition)}/${formatTime(duration)}"
    }

    private fun formatTime(ms: Int): String {
        val seconds = round(ms.toDouble() / 1000).toInt()
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.CANADA, "%02d:%02d", mins, secs)
    }
}