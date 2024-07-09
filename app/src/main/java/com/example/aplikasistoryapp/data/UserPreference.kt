package com.example.aplikasistoryapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserPreference {
    private const val USER_TOKEN = "user_token"

    private lateinit var sharedPreferences: SharedPreferences
    private val _userToken = MutableStateFlow("")

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        _userToken.value = sharedPreferences.getString(USER_TOKEN, "") ?: ""
    }

    fun setUserToken(token: String) {
        _userToken.value = token
        sharedPreferences.edit().putString(USER_TOKEN, token).apply()
    }

    fun getUserToken() = _userToken.asStateFlow()
}