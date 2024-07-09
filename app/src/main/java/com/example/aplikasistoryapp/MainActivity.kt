package com.example.aplikasistoryapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasistoryapp.data.Injection
import com.example.aplikasistoryapp.ui.LoginActivity
import com.example.aplikasistoryapp.ui.viewmodel.StoryViewModel
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import com.example.aplikasistoryapp.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var storyAdapter: StoryAdapter

    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences("user_session", MODE_PRIVATE)

        if (!isLoggedIn()) {
            navigateToLogin()
            return
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        storyAdapter = StoryAdapter(emptyList())
        recyclerView.adapter = storyAdapter

        observeStories()
        storyViewModel.fetchStories(getToken())
    }

    private fun observeStories() {
        lifecycleScope.launch {
            storyViewModel.stories.collect {
                it?.let { storyResponse ->
                    storyAdapter.submitList(storyResponse.listStory)
                }
            }
        }
    }
    private fun isLoggedIn(): Boolean {
        val token = preferences.getString("token", null)
        return token != null
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
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

    private fun logout() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
        navigateToLogin()
    }

    private fun getToken(): String {
        return preferences.getString("token", "") ?: ""
    }
}