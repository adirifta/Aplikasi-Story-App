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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

@Suppress("DEPRECATION")
class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var photoUri: Uri? = null
    private var currentLocation: Location? = null
    private lateinit var checkboxAddLocation: ImageView
    private var isLocationChecked = false

    private val addStoryViewModel: AddStoryViewModel by viewModels {
        AddViewModelFactory(Injection.provideRepository(this))
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            binding.ivAddPhoto.setImageURI(photoUri)
        } else {
            photoUri = null
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            photoUri = data?.data ?: photoUri
            binding.ivAddPhoto.setImageURI(photoUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.buttonAddGallery.setOnClickListener {
            openGallery()
        }

        binding.buttonAddCamera.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        checkboxAddLocation = binding.checkboxAddLocation
        checkboxAddLocation.setOnClickListener {
            toggleLocationCheckbox()
        }

        binding.buttonAdd.setOnClickListener {
            uploadStory()
        }

        playAnimation()

        binding.backButton.setOnClickListener {
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

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()  // Re-attempt to get location if permissions are granted
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
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
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun toggleLocationCheckbox() {
        isLocationChecked = !isLocationChecked
        val drawableRes = if (isLocationChecked) R.drawable.ic_checked else R.drawable.ic_unchecked
        checkboxAddLocation.setImageResource(drawableRes)

        if (isLocationChecked) {
            getCurrentLocation()
        } else {
            currentLocation = null
        }
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString().trim()
        if (description.isEmpty()) {
            binding.edAddDescription.error = "Description is required"
            return
        }

        if (photoUri == null) {
            Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        try {
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
        } catch (e: Exception) {
            Log.e("AddStoryActivity", "File processing error", e)
            Toast.makeText(this, "File processing error: ${e.message}", Toast.LENGTH_SHORT).show()
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.buttonAdd.isEnabled = false
        } else {
            binding.loadingIndicator.visibility = View.GONE
            binding.buttonAdd.isEnabled = true
        }
    }

    companion object {
        private const val MAX_PHOTO_SIZE = 1 * 1024 * 1024 // 1MB
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val REQUEST_LOCATION_PERMISSION = 102
    }

    private fun playAnimation() {
        val textTitleAnimator = ObjectAnimator.ofFloat(binding.edAddDescription, View.ALPHA, 0f, 1f).setDuration(1000)
        val buttonAddAnimator = ObjectAnimator.ofFloat(binding.buttonAdd, View.ALPHA, 0f, 1f).setDuration(1000)
        val buttonAddGalleryAnimator = ObjectAnimator.ofFloat(binding.buttonAddGallery, View.ALPHA, 0f, 1f).setDuration(1000)
        val buttonAddCameraAnimator = ObjectAnimator.ofFloat(binding.buttonAddCamera, View.ALPHA, 0f, 1f).setDuration(1000)
        val checkboxAddLocationAnimator = ObjectAnimator.ofFloat(binding.checkboxAddLocation, View.ALPHA, 0f, 1f).setDuration(700)
        val ivAddPhotoAnimator = ObjectAnimator.ofFloat(binding.ivAddPhoto, View.ALPHA, 0f, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(
                textTitleAnimator,
                buttonAddGalleryAnimator,
                buttonAddCameraAnimator,
                checkboxAddLocationAnimator,
                ivAddPhotoAnimator,
                buttonAddAnimator
            )
            start()
        }
    }
}