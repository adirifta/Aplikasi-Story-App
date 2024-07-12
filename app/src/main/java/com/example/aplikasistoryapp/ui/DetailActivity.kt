package com.example.aplikasistoryapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import com.example.aplikasistoryapp.data.response.Story
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var photo: ImageView
    private lateinit var name: TextView
    private lateinit var description: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        photo = findViewById(R.id.iv_detail_photo)
        name = findViewById(R.id.tv_detail_name)
        description = findViewById(R.id.tv_detail_description)

        // Fetching story by ID
        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        val token = intent.getStringExtra(EXTRA_TOKEN)
        if (storyId != null && token != null) {
            fetchStoryDetail(storyId, token)
        }
    }

    private fun fetchStoryDetail(storyId: String, token: String) {
        Log.d("DetailActivity", "Fetching story detail with token: $token")
        lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService(token)
                val response = apiService.getStoryDetail(storyId)
                if (response.error) {
                    Log.e("DetailActivity", "Error fetching story: ${response.message}")
                } else {
                    response.story?.let {
                        bind(it)
                    } ?: Log.e("DetailActivity", "Story not found")
                }
            } catch (e: Exception) {
                Log.e("DetailActivity", "Exception fetching story", e)
            }
        }
    }

    private fun bind(story: Story) {
        name.text = story.name
        description.text = story.description
        Glide.with(this)
            .load(story.photoUrl)
            .into(photo)
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
        const val EXTRA_TOKEN = "extra_token"
    }
}