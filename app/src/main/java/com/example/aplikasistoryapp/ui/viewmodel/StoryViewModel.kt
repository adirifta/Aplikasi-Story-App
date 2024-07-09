package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.response.ListStoryItem
import com.example.aplikasistoryapp.data.response.StoryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _stories = MutableStateFlow<StoryResponse?>(null)
    val stories: StateFlow<StoryResponse?> = _stories

    fun fetchStories(token: String) {
        viewModelScope.launch {
            try {
                val response = storyRepository.getStories(token)
                _stories.value = response
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}