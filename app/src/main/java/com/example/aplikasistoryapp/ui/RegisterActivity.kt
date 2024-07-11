package com.example.aplikasistoryapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.repository.UserRepository
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.RegisterViewModelFactory
import com.example.aplikasistoryapp.ui.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(UserRepository(ApiConfig.getApiService("")))
    }

    private lateinit var edRegisterName: EditText
    private lateinit var edRegisterEmail: EditText
    private lateinit var edRegisterPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var loadingLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edRegisterName = findViewById(R.id.ed_register_name)
        edRegisterEmail = findViewById(R.id.ed_register_email)
        edRegisterPassword = findViewById(R.id.ed_register_password)
        btnRegister = findViewById(R.id.btn_register)
        loadingLayout = findViewById(R.id.loading_layout)

        btnRegister.setOnClickListener {
            val name = edRegisterName.text.toString()
            val email = edRegisterEmail.text.toString()
            val password = edRegisterPassword.text.toString()

            if (validateInput(name, email, password)) {
                registerViewModel.register(name, email, password)
            }
        }

        registerViewModel.registerResponse.observe(this) { response ->
            if (response.error == true) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        registerViewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        registerViewModel.isLoading.observe(this) { isLoading ->
            loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
            edRegisterName.isEnabled = !isLoading
            edRegisterEmail.isEnabled = !isLoading
            edRegisterPassword.isEnabled = !isLoading
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 8) {
            edRegisterPassword.error = "Password must be at least 8 characters"
            return false
        }
        return true
    }
}