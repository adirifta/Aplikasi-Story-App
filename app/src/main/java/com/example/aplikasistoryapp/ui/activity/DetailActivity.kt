package com.example.aplikasistoryapp.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.response.Story
import com.example.aplikasistoryapp.ui.viewmodel.DetailViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    private lateinit var photo: ImageView
    private lateinit var name: TextView
    private lateinit var description: TextView
    private lateinit var createdAt: TextView
    private lateinit var loadingIndicator: ProgressBar

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        photo = findViewById(R.id.iv_detail_photo)
        name = findViewById(R.id.tv_detail_name)
        description = findViewById(R.id.tv_detail_description)
        createdAt = findViewById(R.id.tv_detail_created_at)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        val token = intent.getStringExtra(EXTRA_TOKEN)
        if (storyId != null && token != null) {
            viewModel.fetchStoryDetail(storyId, token)
        }

        // Observe LiveData
        viewModel.story.observe(this) { story ->
            story?.let {
                bind(it)
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Log.e("DetailActivity", "Error: $it")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bind(story: Story) {
        name.text = story.name
        description.text = "\t${story.description?.replace("\n", "\n\t")}"
        Glide.with(this)
            .load(story.photoUrl)
            .into(photo)

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm", Locale.getDefault())
            val formattedDate = story.createdAt?.let { inputFormat.parse(it) }
            createdAt.text = formattedDate?.let { outputFormat.format(it) }
        } catch (e: ParseException) {
            Log.e("DetailActivity", "Error parsing date", e)
            createdAt.text = story.createdAt // fallback to original string if parsing fails
        }
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
        const val EXTRA_TOKEN = "extra_token"
    }
}