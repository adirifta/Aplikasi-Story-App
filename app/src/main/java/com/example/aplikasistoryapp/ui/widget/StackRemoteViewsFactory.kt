package com.example.aplikasistoryapp.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.example.aplikasistoryapp.R
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.retrofit.ApiConfig
import com.example.aplikasistoryapp.data.response.ListStoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<ListStoryItem>()

    override fun onCreate() {
        // Do any setup needed here
    }

    override fun onDataSetChanged() {
        // Fetch data from the API
        val token = "YOUR_AUTH_TOKEN" // Replace with your actual token
        val apiService = ApiConfig.getApiService(token)
        val storyRepository = StoryRepository.getInstance(apiService)

        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    val response = storyRepository.getStories()
                    if (response.listStory.isNotEmpty()) {
                        mWidgetItems.clear()
                        mWidgetItems.addAll(response.listStory)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        mWidgetItems.clear()
    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        val story = mWidgetItems[position]

        // Load the image using Glide and set it to the ImageView
        val bitmap: Bitmap = runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    Glide.with(mContext)
                        .asBitmap()
                        .load(story.photoUrl)
                        .submit()
                        .get()
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_image) // Fallback image
                }
            }
        }
        rv.setImageViewBitmap(R.id.imageView, bitmap)

        val extras = bundleOf(
            StoryListWidget.EXTRA_ITEM to position
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = mWidgetItems[position].id?.hashCode()?.toLong() ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}