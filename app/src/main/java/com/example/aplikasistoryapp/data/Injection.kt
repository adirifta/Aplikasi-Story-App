package com.example.aplikasistoryapp.data

import android.content.Context
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        UserPreference.initialize(context)
        val apiService = ApiConfig.getApiService(UserPreference.getUserToken().value)
        return StoryRepository(apiService)
    }
}