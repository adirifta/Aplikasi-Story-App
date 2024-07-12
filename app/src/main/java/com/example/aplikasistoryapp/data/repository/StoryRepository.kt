package com.example.aplikasistoryapp.data.repository

import com.example.aplikasistoryapp.data.response.StoryResponse
import com.example.aplikasistoryapp.data.retrofit.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody

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

    suspend fun addStory(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody?, lon: RequestBody?) =
        apiService.addStory(description, photo, lat, lon)

    suspend fun addStoryGuest(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody?, lon: RequestBody?) =
        apiService.addStoryGuest(description, photo, lat, lon)
}