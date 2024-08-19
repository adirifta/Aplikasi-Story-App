package com.example.aplikasistoryapp.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.Injection
import com.example.aplikasistoryapp.data.response.ListStoryItem
import com.example.aplikasistoryapp.databinding.ActivityMapsBinding
import com.example.aplikasistoryapp.ui.viewmodel.MapsViewModel
import com.example.aplikasistoryapp.ui.viewmodel.viewModelFactory.MapsViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel: MapsViewModel by viewModels {
        MapsViewModelFactory(Injection.provideRepository(this))
    }
    private lateinit var progressBar: ProgressBar

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        progressBar = findViewById(R.id.loadingIndicator)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        showLoading(true)
        mapsViewModel.getStoriesWithLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMapSettings()

        mapsViewModel.storyResponse.observe(this) { storyResponse ->
            showLoading(false)
            if (!storyResponse.error!!) {
                Log.d("MapsActivity", "Stories received: ${storyResponse.listStory.size}")
                addMarkers(storyResponse.listStory)
            } else {
                Log.e("MapsActivity", "Failed to load stories. Error: ${storyResponse.message}")
            }
        }

        // Set custom InfoWindowAdapter
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    private fun setupMapSettings() {
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    private fun addMarkers(stories: List<ListStoryItem>) {
        if (stories.isEmpty()) {
            Log.d("MapsActivity", "No stories available to display.")
            return
        }

        val boundsBuilder = LatLngBounds.Builder()

        stories.forEach { story ->
            val lat = story.lat
            val lon = story.lon

            if (lat != null && lon != null) {
                val latLng = LatLng(lat, lon)
                Log.d("MapsActivity", "Adding marker: Lat $lat, Lon $lon, Name ${story.name}")

                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(story.name)
                        .snippet(story.description)
                )

                // Attach the story to the marker as a tag
                marker?.tag = story

                boundsBuilder.include(latLng)
            }
        }

        if (stories.size > 10) {
            mMap.setMaxZoomPreference(10.0f) // Example zoom level
        }

        if (stories.isNotEmpty()) {
            val bounds: LatLngBounds = boundsBuilder.build()
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    300
                )
            )
        }
    }

    // Custom InfoWindowAdapter class
    inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        @SuppressLint("InflateParams")
        private val window = LayoutInflater.from(this@MapsActivity).inflate(R.layout.custom_marker, null)

        override fun getInfoWindow(marker: Marker): View? {
            // Use default frame
            return null
        }

        override fun getInfoContents(marker: Marker): View {
            val story = marker.tag as? ListStoryItem

            val tvName: TextView = window.findViewById(R.id.tv_item_name)
            val tvDescription: TextView = window.findViewById(R.id.tv_item_description)
            val ivPhoto: ImageView = window.findViewById(R.id.iv_item_photo)

            tvName.text = marker.title
            tvDescription.text = marker.snippet

            // Load image using Glide
            story?.photoUrl?.let {
                Glide.with(this@MapsActivity)
                    .load(it)
                    .into(ivPhoto)
            }

            return window
        }
    }
}