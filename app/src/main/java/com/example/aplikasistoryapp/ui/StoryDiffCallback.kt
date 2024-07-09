package com.example.aplikasistoryapp.ui

import androidx.recyclerview.widget.DiffUtil
import com.example.aplikasistoryapp.data.response.ListStoryItem

class StoryDiffCallback : DiffUtil.ItemCallback<ListStoryItem>() {

    override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
        return oldItem.id == newItem.id // Contoh: Bandingkan ID item
    }

    override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
        return oldItem == newItem // Contoh: Bandingkan keseluruhan konten item
    }
}