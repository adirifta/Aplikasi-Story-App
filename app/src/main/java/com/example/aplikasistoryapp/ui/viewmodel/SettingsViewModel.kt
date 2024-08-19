package com.example.aplikasistoryapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistoryapp.data.UserPreference
import kotlinx.coroutines.launch

class SettingsViewModel(private val userPreference: UserPreference) : ViewModel() {

    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> get() = _logoutStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun logout() {
        viewModelScope.launch {
            try {
                userPreference.clearUserToken()
                _logoutStatus.postValue(true)
            } catch (e: Exception) {
                _logoutStatus.postValue(false)
            }
        }
    }
}