package com.example.aplikasistoryapp.data.repository

import com.example.aplikasistoryapp.data.response.ListStoryItem
import com.example.aplikasistoryapp.data.response.StoryResponse
import com.example.aplikasistoryapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class StoryRepository(private val apiService: ApiService) {
    suspend fun getStories(token: String): StoryResponse {
        return apiService.getStories(token)
    }
}