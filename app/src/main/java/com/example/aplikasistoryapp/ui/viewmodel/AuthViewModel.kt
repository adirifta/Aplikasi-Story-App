package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.repository.UserRepository
import com.example.aplikasistoryapp.data.response.LoginResponse
import com.example.aplikasistoryapp.data.response.RegisterResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

open class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()

    fun handleHttpException(e: HttpException) {
        errorMessage.postValue(e.response()?.errorBody()?.string())
    }
}