package com.example.aplikasistoryapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistoryapp.MainActivity
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import com.example.aplikasistoryapp.data.repository.UserRepository
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import com.example.aplikasistoryapp.ui.viewmodel.LoginViewModel
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.LoginViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(UserRepository(ApiConfig.getApiService("")))
    }

    private lateinit var edLoginEmail: EditText
    private lateinit var edLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var loadingLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edLoginEmail = findViewById(R.id.ed_login_email)
        edLoginPassword = findViewById(R.id.ed_login_password)
        btnLogin = findViewById(R.id.btn_login)
        loadingLayout = findViewById(R.id.loading_layout)

        btnLogin.setOnClickListener {
            val email = edLoginEmail.text.toString()
            val password = edLoginPassword.text.toString()

            if (validateInput(email, password)) {
                loginViewModel.login(email, password)
            }
        }

        loginViewModel.loginResponse.observe(this) { response ->
            if (response.error) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    saveSession(response.loginResult.token)
                    navigateToMain()
                }
            }
        }

        loginViewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        loginViewModel.isLoading.observe(this) { isLoading ->
            loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private suspend fun saveSession(token: String) {
        UserPreference.getInstance(dataStore).setUserToken(token)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}