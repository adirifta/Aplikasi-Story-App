package com.example.aplikasistoryapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikasistoryapp.MainActivity
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.repository.UserRepository
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import com.example.aplikasistoryapp.ui.viewmodel.AuthViewModel
import com.example.aplikasistoryapp.ui.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepository(ApiConfig.getApiService("")))
    }

    private lateinit var edLoginEmail: EditText
    private lateinit var edLoginPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edLoginEmail = findViewById(R.id.ed_login_email)
        edLoginPassword = findViewById(R.id.ed_login_password)
        btnLogin = findViewById(R.id.btn_login)
//        btnRegister = findViewById(R.id.btn_register)

        btnLogin.setOnClickListener {
            val email = edLoginEmail.text.toString()
            val password = edLoginPassword.text.toString()

            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }

        authViewModel.loginResponse.observe(this) { response ->
            if (response.error) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            } else {
                saveSession(response.loginResult.token)
                navigateToMain()
            }
        }

        authViewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun saveSession(token: String) {
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        preferences.edit().apply {
            putString("token", token)
            apply()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        // Clear session token when activity is destroyed
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        preferences.edit().remove("token").apply()
        super.onDestroy()
    }
}