package com.example.aplikasistoryapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasistoryapp.data.Injection
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import com.example.aplikasistoryapp.ui.LoginActivity
import com.example.aplikasistoryapp.ui.viewmodel.StoryViewModel
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var loadingLayout: View

    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadingLayout = findViewById(R.id.loading_layout)

        storyAdapter = StoryAdapter()
        recyclerView.adapter = storyAdapter

        if (!isLoggedIn()) {
            navigateToLogin()
            return
        }

        observeStories()
        showLoading(true)
        storyViewModel.fetchStories()
    }

    private fun observeStories() {
        lifecycleScope.launch {
            storyViewModel.stories.collect {
                it?.let { storyResponse ->
                    Log.d("MainActivity", "Stories received: ${storyResponse.listStory.size}")
                    storyAdapter.submitList(storyResponse.listStory)
                    showLoading(false)
                }
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        val token = runBlocking { UserPreference.getInstance(dataStore).getUserToken().first() }
        Log.d("MainActivity", "User token: $token")
        return token.isNotEmpty()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            loadingLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_logout -> {
//                logout()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    private fun logout() {
//        val editor = preferences.edit()
//        editor.clear()
//        editor.apply()
//        navigateToLogin()
//    }
}