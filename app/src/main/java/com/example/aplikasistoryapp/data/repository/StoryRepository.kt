package com.example.aplikasistoryapp.data.repository

import com.example.aplikasistoryapp.data.response.StoryResponse
import com.example.aplikasistoryapp.data.retrofit.ApiService

class StoryRepository(private val apiService: ApiService) {
    suspend fun getStories(): StoryResponse {
        return apiService.getStories()
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService).also { instance = it }
            }
    }
}