package com.example.aplikasistoryapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        checkLoginStatus()

        observeViewModel()
        setupListeners()

        storyViewModel.fetchStories()

        swipeRefreshLayout.setOnRefreshListener {
            storyViewModel.fetchStories()
        }
    }

    private fun initViews() {
        loadingIndicator = findViewById(R.id.loadingIndicator)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        storyAdapter = StoryAdapter { story ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_STORY_ID, story.id)
                putExtra(DetailActivity.EXTRA_TOKEN, getUserToken())
            }
            startActivity(intent)
        }
        binding.recyclerView.adapter = storyAdapter
    }

    private fun observeViewModel() {
        storyViewModel.stories.observe(this) { storyResponse ->
            storyResponse?.let {
                Log.d("MainActivity", "Stories received: ${it.listStory.size}")
                storyAdapter.submitList(it.listStory)
                swipeRefreshLayout.isRefreshing = false
            }
        }

        storyViewModel.isLoading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            addStoryResultLauncher.launch(intent)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun checkLoginStatus() {
        if (!isLoggedIn()) {
            navigateToLogin()
        }
    }

    private fun isLoggedIn(): Boolean {
        val token = runBlocking { UserPreference.getInstance(dataStore).getUserToken().firstOrNull() }
        Log.d("MainActivity", "User token: $token")
        return !token.isNullOrEmpty()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun getUserToken(): String? {
        return runBlocking { UserPreference.getInstance(dataStore).getUserToken().firstOrNull() }
    }

    private val addStoryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            storyViewModel.fetchStories()
        }
    }
}