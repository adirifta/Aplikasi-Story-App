package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.response.StoryResponse
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _storyResponse = MutableLiveData<StoryResponse>()
    val storyResponse: LiveData<StoryResponse> get() = _storyResponse

    fun getStoriesWithLocation() {
        viewModelScope.launch {
            try {
                val response = repository.getStoriesWithLocation()
                _storyResponse.postValue(response)
            } catch (e: Exception) {
                _storyResponse.postValue(e.message?.let { StoryResponse(error = true, message = it) })
            }
        }
    }
}