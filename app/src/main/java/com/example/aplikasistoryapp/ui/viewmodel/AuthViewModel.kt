package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.UserRepository
import com.example.aplikasistoryapp.data.response.LoginResponse
import com.example.aplikasistoryapp.data.response.RegisterResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    val registerResponse = MutableLiveData<RegisterResponse>()
    val loginResponse = MutableLiveData<LoginResponse>()
    val errorMessage = MutableLiveData<String>()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.register(name, email, password)
                registerResponse.postValue(response)
            } catch (e: HttpException) {
                errorMessage.postValue(e.response()?.errorBody()?.string())
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.login(email, password)
                loginResponse.postValue(response)
            } catch (e: HttpException) {
                errorMessage.postValue(e.response()?.errorBody()?.string())
            }
        }
    }
}