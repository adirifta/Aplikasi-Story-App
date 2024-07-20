package com.example.aplikasistoryapp.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.Injection
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import com.example.aplikasistoryapp.databinding.ActivityAddStoryBinding
import com.example.aplikasistoryapp.ui.viewmodel.AddStoryViewModel
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.AddViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddStoryActivity : AppCompatActivity() {

    private lateinit var descriptionEditText: EditText
    private lateinit var addPhotoImageView: ImageView
    private lateinit var addGalleryButton: Button
    private lateinit var addCameraButton: Button
    private lateinit var addButton: Button
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private lateinit var loadingLayout: View

    private lateinit var binding: ActivityAddStoryBinding

    private val addStoryViewModel: AddStoryViewModel by viewModels {
        AddViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        descriptionEditText = findViewById(R.id.ed_add_description)
        addPhotoImageView = findViewById(R.id.iv_add_photo)
        addGalleryButton = findViewById(R.id.button_add_gallery)
        addCameraButton = findViewById(R.id.button_add_camera)
        addButton = findViewById(R.id.button_add)
        loadingLayout = findViewById(R.id.loading_layout)

        addGalleryButton.setOnClickListener {
            openGallery()
        }

        addCameraButton.setOnClickListener {
            openCamera()
        }

        addButton.setOnClickListener {
            uploadStory()
        }

        playAnimation()

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        addStoryViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        addStoryViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        if (hasCameraPermission()) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("AddStoryActivity", "Error occurred while creating the file", ex)
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.aplikasistoryapp.fileprovider",
                        it
                    )
                    photoUri = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    resultLauncher.launch(takePictureIntent)
                }
            }
        } else {
            requestCameraPermission()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            photoUri = data?.data ?: photoUri
            addPhotoImageView.setImageURI(photoUri)
        }
    }

    private fun uploadStory() {
        val description = descriptionEditText.text.toString().trim()
        if (description.isEmpty()) {
            descriptionEditText.error = "Description is required"
            return
        }

        if (photoUri == null) {
            Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val contentResolver = contentResolver
        val inputStream = contentResolver.openInputStream(photoUri!!)
        val photoFile = createTempFile()

        inputStream?.use { input ->
            photoFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (photoFile.length() > MAX_PHOTO_SIZE) {
            Toast.makeText(this, "Photo size exceeds 1MB", Toast.LENGTH_SHORT).show()
            return
        }

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val photoRequestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody)

        lifecycleScope.launch {
            val token = UserPreference.getInstance(dataStore).getUserToken().firstOrNull()
            try {
                token?.let {
                    addStoryViewModel.addStory(descriptionRequestBody, photoPart)
                } ?: addStoryViewModel.addStoryGuest(descriptionRequestBody, photoPart)
                finish()
            } catch (e: HttpException) {
                Log.e("AddStoryActivity", "HTTP Exception: ${e.code()}")
                Toast.makeText(this@AddStoryActivity, "Error: ${e.message()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("AddStoryActivity", "Error uploading story", e)
                Toast.makeText(this@AddStoryActivity, "Error uploading story", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingLayout.visibility = View.VISIBLE
            addButton.isEnabled = false  // Disable button while loading
        } else {
            loadingLayout.visibility = View.GONE
            addButton.isEnabled = true  // Enable button after loading finished
        }
    }

    companion object {
        private const val MAX_PHOTO_SIZE = 1 * 1024 * 1024 // 1MB
        private const val REQUEST_CAMERA_PERMISSION = 101
    }

    private fun playAnimation() {
        val textTitleAnimator = ObjectAnimator.ofFloat(binding.edAddDescription, View.ALPHA, 0f, 1f).setDuration(1000)
        val galleryButtonAnimator = ObjectAnimator.ofFloat(binding.buttonAddGallery, View.ALPHA, 0f, 1f).setDuration(1000)
        val cameraButtonAnimator = ObjectAnimator.ofFloat(binding.buttonAddCamera, View.ALPHA, 0f, 1f).setDuration(1000)
        val addButtonAnimator = ObjectAnimator.ofFloat(binding.buttonAdd, View.ALPHA, 0f, 1f).setDuration(1000)

        AnimatorSet().apply {
            playSequentially(textTitleAnimator, galleryButtonAnimator, cameraButtonAnimator, addButtonAnimator)
            start()
        }
    }
}