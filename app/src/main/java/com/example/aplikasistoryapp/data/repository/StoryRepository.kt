package com.example.aplikasistoryapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.aplikasistoryapp.data.StoryPagingSource
import com.example.aplikasistoryapp.data.response.ListStoryItem
import com.example.aplikasistoryapp.data.response.StoryResponse
import com.example.aplikasistoryapp.data.retrofit.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(private val apiService: ApiService) {
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
//                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService) }
        ).liveData
    }

    companion object {
        @Volatile private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService).also { instance = it }
            }
    }

    suspend fun addStory(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody?, lon: RequestBody?) =
        apiService.addStory(description, photo, lat, lon)

    suspend fun addStoryGuest(description: RequestBody, photo: MultipartBody.Part, lat: RequestBody?, lon: RequestBody?) =
        apiService.addStoryGuest(description, photo, lat, lon)

    suspend fun getStoriesWithLocation(): StoryResponse {
        return apiService.getStoriesWithLocation()
    }

    suspend fun getStoriesAsList(): List<ListStoryItem> {
        return try {
            val response = apiService.getStories()
            response.listStory
        } catch (e: Exception) {
            emptyList()
        }
    }
}