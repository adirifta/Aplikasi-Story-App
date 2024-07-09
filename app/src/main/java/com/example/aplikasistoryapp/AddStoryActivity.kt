package com.example.aplikasistoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddStoryActivity : AppCompatActivity() {

    private lateinit var ivPhoto: ImageView
    private lateinit var edDescription: EditText
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        ivPhoto = findViewById(R.id.iv_add_photo)
        edDescription = findViewById(R.id.ed_add_description)
        btnAdd = findViewById(R.id.button_add)

        ivPhoto.setOnClickListener {
            selectPhotoFromGallery()
        }

        btnAdd.setOnClickListener {
            val description = edDescription.text.toString()
            if (description.isNotEmpty()) {
                uploadStory(description)
            } else {
                edDescription.error = "Description cannot be empty"
            }
        }
    }

    private fun selectPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    private fun uploadStory(description: String) {
        // Replace with actual upload logic
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}