package com.example.aplikasistoryapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.Injection
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import com.example.aplikasistoryapp.databinding.ActivityMainBinding
import com.example.aplikasistoryapp.ui.activity.AddStoryActivity
import com.example.aplikasistoryapp.ui.activity.DetailActivity
import com.example.aplikasistoryapp.ui.activity.LoginActivity
import com.example.aplikasistoryapp.ui.activity.SettingsActivity
import com.example.aplikasistoryapp.ui.viewmodel.StoryViewModel
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var loadingIndicator: ProgressBar

    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingIndicator = findViewById(R.id.loadingIndicator)
        loadingIndicator.visibility = View.VISIBLE
        setupRecyclerView()

        if (!isLoggedIn()) {
            navigateToLogin()
            return
        }

        observeStories()

        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivityForResult(intent, ADD_STORY_REQUEST_CODE)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observeStories()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        storyAdapter = StoryAdapter { story ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_STORY_ID, story.id)
            intent.putExtra(DetailActivity.EXTRA_TOKEN, runBlocking { UserPreference.getInstance(dataStore).getUserToken().first() })
            startActivity(intent)
        }
        binding.recyclerView.adapter = storyAdapter
    }

    private fun observeStories() {
        storyViewModel.stories.observe(this) { storyResponse ->
            loadingIndicator.visibility = View.GONE
            storyResponse?.let {
                Log.d("MainActivity", "Stories received: ${storyResponse.listStory.size}")
                storyAdapter.submitList(storyResponse.listStory)
            }
        }

        storyViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                loadingIndicator.visibility = View.VISIBLE
            } else {
                loadingIndicator.visibility = View.GONE
            }
        }

        storyViewModel.fetchStories()
    }

    private fun isLoggedIn(): Boolean {
        val token = runBlocking { UserPreference.getInstance(dataStore).getUserToken().firstOrNull() }
        Log.d("MainActivity", "User token: $token")
        return !token.isNullOrEmpty()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val ADD_STORY_REQUEST_CODE = 1
    }
}