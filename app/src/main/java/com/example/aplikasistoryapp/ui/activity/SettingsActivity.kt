package com.example.aplikasistoryapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import com.example.aplikasistoryapp.ui.viewmodel.SettingsViewModel
import android.provider.Settings
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.SettingsViewModelFactory

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton: ImageView = findViewById(R.id.backButton)
        val languageArrow: ImageView = findViewById(R.id.languageArrow)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(UserPreference.getInstance(dataStore))
        )[SettingsViewModel::class.java]

        backButton.setOnClickListener {
            finish()
        }

        languageArrow.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        // Observe logout status
        settingsViewModel.logoutStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Handle error
            }
        }

        // Handle logout action
        logoutButton.setOnClickListener {
            settingsViewModel.logout()
        }
    }

    fun isLoading(): Boolean {
        return settingsViewModel.isLoading.value ?: false
    }
}