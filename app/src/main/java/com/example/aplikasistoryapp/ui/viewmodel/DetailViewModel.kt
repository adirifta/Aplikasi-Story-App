package com.example.aplikasistoryapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.response.Story
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _story = MutableLiveData<Story?>()
    val story: LiveData<Story?> get() = _story

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchStoryDetail(storyId: String, token: String) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val apiService = ApiConfig.getApiService(token)
                val response = apiService.getStoryDetail(storyId)
                if (response.error) {
                    _error.value = response.message
                } else {
                    _story.value = response.story
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}