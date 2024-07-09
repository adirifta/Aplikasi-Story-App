package com.example.aplikasistoryapp.data.repository

import com.example.aplikasistoryapp.data.retrofit.ApiService

class UserRepository(private val apiService: ApiService) {
    suspend fun register(name: String, email: String, password: String) = apiService.register(name, email, password)
    suspend fun login(email: String, password: String) = apiService.login(email, password)
}