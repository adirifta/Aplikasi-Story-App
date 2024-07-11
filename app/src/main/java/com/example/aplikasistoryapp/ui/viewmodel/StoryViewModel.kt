package com.example.aplikasistoryapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.response.StoryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _stories = MutableStateFlow<StoryResponse?>(null)
    val stories: StateFlow<StoryResponse?> = _stories

    fun fetchStories() {
        viewModelScope.launch {
            try {
                val response = storyRepository.getStories()
                _stories.value = response
                Log.d("StoryViewModel", "Stories fetched: ${response.listStory.size}")
            } catch (e: Exception) {
                Log.e("StoryViewModel", "Error fetching stories", e)
            }
        }
    }
}