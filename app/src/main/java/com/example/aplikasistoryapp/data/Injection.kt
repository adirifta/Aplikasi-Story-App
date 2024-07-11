package com.example.aplikasistoryapp.data

import android.content.Context
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val userToken = runBlocking { pref.getUserToken().first() }
        val apiService = ApiConfig.getApiService(userToken)
        return StoryRepository.getInstance(apiService)
    }
}
