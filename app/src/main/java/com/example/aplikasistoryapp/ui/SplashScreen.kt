package com.example.aplikasistoryapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistoryapp.MainActivity
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import com.example.aplikasistoryapp.ui.activity.WelcomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                if (isLoggedIn()) {
                    navigateToMain()
                } else {
                    navigateToLogin()
                }
                finish()
            }
        }, 2500)

        supportActionBar?.hide()
    }

    private suspend fun isLoggedIn(): Boolean {
        val token = UserPreference.getInstance(dataStore).getUserToken().first()
        return token.isNotEmpty()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, WelcomeActivity::class.java))
    }
}