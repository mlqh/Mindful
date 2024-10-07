package com.ece452s24g7.mindful.adapters

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ece452s24g7.mindful.R
import com.ece452s24g7.mindful.activities.CreateEntryActivity
import com.ece452s24g7.mindful.ai.Ai
import com.ece452s24g7.mindful.ai.AiAdapter
import com.ece452s24g7.mindful.ai.AiPromptSet
import com.ece452s24g7.mindful.database.Entry
import com.ece452s24g7.mindful.database.EntryDatabase
import com.ece452s24g7.mindful.views.AudioPlayerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class EntryListAdapter(
    private val entries: List<Entry>,
    private val context: Context,
    private val onEntryChanged: () -> Unit
) : RecyclerView.Adapter<EntryListAdapter.ViewHolder>() {

    private val dao = EntryDatabase.getInstance(context.applicationContext as Application).dao()
    private val ai = Ai(AiAdapter.getCohere(), AiPromptSet.getDefault())
    private lateinit var sharedPreferences: SharedPreferences

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateView: TextView = view.findViewById(R.id.entry_list_item_date_view)
        val bodyView: TextView = view.findViewById(R.id.entry_list_item_body_view)
        val threeDotButton: FloatingActionButton = view.findViewById(R.id.three_dot_menu_button)
        val exitSummaryButton: Button = view.findViewById(R.id.entry_list_item_exit_summary_button)
        val aiProgress: ProgressBar = view.findViewById(R.id.entry_list_item_summary_progress)
        val photoVideoList: RecyclerView = view.findViewById(R.id.entry_list_item_photo_video_list)
        val audioPlayer: AudioPlayerView = view.findViewById(R.id.entry_list_item_audio_player)
        val locationView: TextView = view.findViewById(R.id.entry_list_item_location_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.entry_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        val sdf = SimpleDateFormat("EEEE MMMM d yyyy, h:mm a")

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val showAiSummary = sharedPreferences.getBoolean("ai_summary", false)
        val showAiInsights = sharedPreferences.getBoolean("ai_insight", false)
        val editLockDuration = sharedPreferences.getString("edit_lock", "15")

        val canEdit = (entry.date.time + (editLockDuration?.toInt() ?: 15) * 60 * 1000) > System.currentTimeMillis()

        holder.dateView.text = sdf.format(entry.date)
        holder.bodyView.text = entry.text
        if (entry.location != null) {
            holder.locationView.text = entry.location
            holder.locationView.visibility = View.VISIBLE
        }

        holder.photoVideoList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        holder.photoVideoList.adapter = PhotoVideoListAdapter(entry.imageURIs, entry.videoURIs)

        holder.audioPlayer.setFilePath(entry.audioPath)

        holder.threeDotButton.setOnClickListener {
            val popup = PopupMenu(holder.threeDotButton.context, holder.threeDotButton)
            popup.inflate(R.menu.three_dot_menu)

            if (!showAiSummary) {
                popup.menu.removeItem(R.id.three_dot_menu_summarize)
            }

            if (!showAiInsights) {
                popup.menu.removeItem(R.id.three_dot_menu_insights)
            }

            if (!canEdit) {
                popup.menu.removeItem(R.id.three_dot_menu_edit)
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.three_dot_menu_edit -> {
                        val editEntryIntent = Intent(context, CreateEntryActivity::class.java)
                        editEntryIntent.putExtra("entry_id", entry.uid)
                        editEntryIntent.putExtra("entry_date", entry.date.time)
                        editEntryIntent.putExtra("entry_body", entry.text)
                        editEntryIntent.putExtra("entry_imageURIs", entry.imageURIs.toTypedArray())
                        editEntryIntent.putExtra("entry_videoURIs", entry.videoURIs.toTypedArray())
                        editEntryIntent.putExtra("entry_audioPath", entry.audioPath)
                        editEntryIntent.putExtra("entry_location", entry.location)
                        context.startActivity(editEntryIntent)
                        true
                    }
                    R.id.three_dot_menu_delete -> {
                        deleteEntry(entry)
                        true
                    }
                    R.id.three_dot_menu_summarize -> {
                        handleAiOption(holder, entry.text ?: "", context.getString(R.string.summarizing)) {
                            ai.getSummary(entry.text ?: "")
                        }
                        true
                    }
                    R.id.three_dot_menu_insights -> {
                        handleAiOption(holder, entry.text ?: "", context.getString(R.string.getting_insights)) {
                            ai.getInsights(entry.text ?: "")
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun handleAiOption(holder: ViewHolder, entryText: String, loadingText: String, getAiResponse: () -> String) {
        holder.bodyView.textSize = 20f
        holder.bodyView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        holder.bodyView.text = loadingText
        holder.aiProgress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val aiResponse =  try {
                getAiResponse()
            } catch (e: Exception) {
                context.getString(R.string.ai_response_fail)
            }

            withContext(Dispatchers.Main) {
                holder.bodyView.textSize = 16f
                holder.bodyView.textAlignment = View.TEXT_ALIGNMENT_INHERIT
                holder.bodyView.text = aiResponse
                holder.aiProgress.visibility = View.GONE
                holder.exitSummaryButton.setOnClickListener {
                    holder.bodyView.text = entryText
                    holder.exitSummaryButton.visibility = View.GONE
                }
                holder.exitSummaryButton.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = entries.size

    private fun deleteEntry(entry: Entry) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(entry)
            withContext(Dispatchers.Main) {
                onEntryChanged()
            }
        }
    }
}
