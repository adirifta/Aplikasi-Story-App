package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.UserRepository
import com.example.aplikasistoryapp.data.response.RegisterResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val userRepository: UserRepository) : AuthViewModel(userRepository) {
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> get() = _registerResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = userRepository.register(name, email, password)
                _registerResponse.postValue(response)
            } catch (e: HttpException) {
                handleHttpException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}