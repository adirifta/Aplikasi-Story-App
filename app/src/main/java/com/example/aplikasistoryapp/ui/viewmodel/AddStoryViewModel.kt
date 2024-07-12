package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun addStory(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody? = null, lon: RequestBody? = null) {
        viewModelScope.launch {
            storyRepository.addStory(description, photo, lat, lon)
        }
    }

    fun addStoryGuest(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody? = null, lon: RequestBody? = null) {
        viewModelScope.launch {
            storyRepository.addStoryGuest(description, photo, lat, lon)
        }
    }
}