package com.example.aplikasistoryapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.response.StoryResponse
import kotlinx.coroutines.launch

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<StoryResponse>()
    val stories: LiveData<StoryResponse> get() = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchStories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = storyRepository.getStories()
                _stories.value = response
                Log.d("StoryViewModel", "Stories fetched: ${response.listStory.size}")
            } catch (e: Exception) {
                Log.e("StoryViewModel", "Error fetching stories", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}