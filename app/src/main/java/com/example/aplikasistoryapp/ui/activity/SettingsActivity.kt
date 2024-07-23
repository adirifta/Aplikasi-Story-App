package com.example.aplikasistoryapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import kotlinx.coroutines.launch
import android.provider.Settings

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton: ImageView = findViewById(R.id.backButton)
        val languageArrow: ImageView = findViewById(R.id.languageArrow)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        backButton.setOnClickListener {
            finish()
        }

        languageArrow.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        // Handle logout action
        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                UserPreference.getInstance(dataStore).clearUserToken()
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}