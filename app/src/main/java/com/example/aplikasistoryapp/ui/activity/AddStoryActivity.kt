package com.example.aplikasistoryapp.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.Injection
import com.example.aplikasistoryapp.data.UserPreference
import com.example.aplikasistoryapp.data.dataStore
import com.example.aplikasistoryapp.databinding.ActivityAddStoryBinding
import com.example.aplikasistoryapp.ui.getImageUri
import com.example.aplikasistoryapp.ui.viewmodel.AddStoryViewModel
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.AddViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class AddStoryActivity : AppCompatActivity() {

    private lateinit var descriptionEditText: EditText
    private lateinit var addPhotoImageView: ImageView
    private lateinit var addGalleryButton: Button
    private lateinit var addCameraButton: Button
    private lateinit var addButton: Button
    private lateinit var locationCheckBox: CheckBox
    private var photoUri: Uri? = null
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private lateinit var binding: ActivityAddStoryBinding

    private val addStoryViewModel: AddStoryViewModel by viewModels {
        AddViewModelFactory(Injection.provideRepository(this))
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            addPhotoImageView.setImageURI(photoUri)
        } else {
            photoUri = null
        }
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
        locationCheckBox = findViewById(R.id.checkbox_add_location)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        loadingIndicator.visibility = View.GONE

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        addGalleryButton.setOnClickListener {
            openGallery()
        }

        addCameraButton.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        addButton.setOnClickListener {
            if (locationCheckBox.isChecked) {
                getCurrentLocation()
            } else {
                uploadStory()
            }
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

    private fun openCamera() {
        photoUri = getImageUri(this)
        takePictureLauncher.launch(photoUri!!)
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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()  // Re-attempt to get location if permissions are granted
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            photoUri = data?.data ?: photoUri
            addPhotoImageView.setImageURI(photoUri)
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            currentLocation = location
            uploadStory()  // Proceed to upload story after getting location
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
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
            showLoading(false)
            return
        }

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val photoRequestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody)
        val latRequestBody = currentLocation?.latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lonRequestBody = currentLocation?.longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        lifecycleScope.launch {
            val token = UserPreference.getInstance(dataStore).getUserToken().firstOrNull()
            try {
                if (token != null) {
                    addStoryViewModel.addStory(descriptionRequestBody, photoPart, latRequestBody, lonRequestBody)
                } else {
                    addStoryViewModel.addStoryGuest(descriptionRequestBody, photoPart, latRequestBody, lonRequestBody)
                }
                setResult(Activity.RESULT_OK)

                sendBroadcast(Intent("com.example.aplikasistoryapp.NEW_STORY_ADDED"))
                finish()
                Toast.makeText(this@AddStoryActivity, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
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
            loadingIndicator.visibility = View.VISIBLE
            addButton.isEnabled = false  // Disable button while loading
        } else {
            loadingIndicator.visibility = View.GONE
            addButton.isEnabled = true  // Enable button after loading finished
        }
    }

    companion object {
        private const val MAX_PHOTO_SIZE = 1 * 1024 * 1024 // 1MB
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val REQUEST_LOCATION_PERMISSION = 102
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