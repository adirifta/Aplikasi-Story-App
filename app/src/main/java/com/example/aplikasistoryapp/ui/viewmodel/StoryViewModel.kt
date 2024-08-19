package com.example.aplikasistoryapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.response.ListStoryItem
import kotlinx.coroutines.launch

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<PagingData<ListStoryItem>>()
    val stories: LiveData<PagingData<ListStoryItem>> get() = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        fetchStories()
    }

    fun fetchStories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Here, observe the paging data from the repository
                storyRepository.getStories().cachedIn(viewModelScope).observeForever { pagingData ->
                    _stories.postValue(pagingData)
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("StoryViewModel", "Error fetching stories", e)
                _isLoading.postValue(false)
            }
        }
    }
}