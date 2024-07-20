package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun addStory(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody? = null, lon: RequestBody? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                storyRepository.addStory(description, photo, lat, lon)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addStoryGuest(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody? = null, lon: RequestBody? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                storyRepository.addStoryGuest(description, photo, lat, lon)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}