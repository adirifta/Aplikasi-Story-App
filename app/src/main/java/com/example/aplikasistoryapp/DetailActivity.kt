//package com.example.aplikasistoryapp
//
//import android.os.Bundle
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class DetailActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_detail)
//
//        val story = intent.getParcelableExtra<Story>("story") ?: return
//
//        val tvName: TextView = findViewById(R.id.tv_detail_name)
//        val ivPhoto: ImageView = findViewById(R.id.iv_detail_photo)
//        val tvDescription: TextView = findViewById(R.id.tv_detail_description)
//
//        tvName.text = story.userName
//        Glide.with(this).load(story.photoUrl).into(ivPhoto)
//        tvDescription.text = story.description
//    }
//}