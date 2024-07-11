package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.UserRepository
import com.example.aplikasistoryapp.data.response.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val userRepository: UserRepository) : AuthViewModel(userRepository) {
    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> get() = _loginResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = userRepository.login(email, password)
                _loginResponse.postValue(response)
            } catch (e: HttpException) {
                handleHttpException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}