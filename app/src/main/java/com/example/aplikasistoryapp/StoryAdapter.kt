package com.example.aplikasistoryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasistoryapp.data.response.ListStoryItem
import com.bumptech.glide.Glide
import com.example.aplikasistoryapp.ui.StoryDiffCallback

class StoryAdapter(emptyList: List<Any>) : ListAdapter<ListStoryItem, StoryAdapter.ViewHolder>(StoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvItemName: TextView = view.findViewById(R.id.tv_item_name)
        private val tvItemDescription: TextView = view.findViewById(R.id.tv_item_description)
        private val ivItemPhoto: ImageView = view.findViewById(R.id.iv_item_photo)

        fun bind(story: ListStoryItem) {
            tvItemName.text = story.name
            tvItemDescription.text = story.description
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(ivItemPhoto)
        }
    }
}