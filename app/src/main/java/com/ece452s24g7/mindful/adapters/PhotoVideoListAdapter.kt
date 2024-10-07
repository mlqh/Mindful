package com.ece452s24g7.mindful.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ece452s24g7.mindful.R

class PhotoVideoListAdapter(
    private val imageURIs : List<String>,
    private val videoURIs : List<String>
) : RecyclerView.Adapter<PhotoVideoListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photoView: ImageView = view.findViewById(R.id.photo_view)
        val videoView: VideoView = view.findViewById(R.id.video_view)
        val frame: FrameLayout = view.findViewById(R.id.photo_video_frame)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_video_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < imageURIs.size) {
            val image = imageURIs[position]
            // holder.photoView.setImageURI(Uri.parse(image))
            Glide.with(holder.itemView.context).load(Uri.parse(image)).into(holder.photoView)
        } else {
            val video = videoURIs[position - imageURIs.size]
            holder.videoView.setVideoURI(Uri.parse(video))
            holder.videoView.visibility = View.VISIBLE
            holder.videoView.setOnPreparedListener {
                holder.videoView.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                holder.videoView.seekTo(0)
                holder.photoView.setImageResource(R.drawable.baseline_play_arrow_48)
            }
            holder.frame.setOnClickListener {
                holder.videoView.start()
                holder.photoView.visibility = View.GONE
            }
            holder.videoView.setOnCompletionListener {
                holder.videoView.seekTo(0)
                holder.photoView.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = imageURIs.size + videoURIs.size

}