package com.example.aplikasistoryapp.ui.viewmodel

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.aplikasistoryapp.DataDummy
import com.example.aplikasistoryapp.MainDispatcherRule
import com.example.aplikasistoryapp.data.repository.StoryRepository
import com.example.aplikasistoryapp.data.response.ListStoryItem
import com.example.aplikasistoryapp.getOrAwaitValue
import com.example.aplikasistoryapp.ui.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // Use the API level you need
class StoryViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var storyViewModel: StoryViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        storyViewModel = StoryViewModel(storyRepository)
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val pagingData = PagingData.from(dummyStories) // Use PagingData.from directly

        // Use a MutableLiveData to simulate the repository response
        val liveData = MutableLiveData<PagingData<ListStoryItem>>()
        liveData.value = pagingData

        // Mock the repository to return the LiveData
        Mockito.`when`(storyRepository.getStories()).thenReturn(liveData)

        // Trigger the fetch
        storyViewModel.fetchStories()

        // Observe the LiveData from the ViewModel
        val actualStories = storyViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val pagingData = PagingData.from(emptyList<ListStoryItem>())
        val liveData = MutableLiveData<PagingData<ListStoryItem>>()
        liveData.value = pagingData

        Mockito.`when`(storyRepository.getStories()).thenReturn(liveData)

        // Trigger the fetch
        storyViewModel.fetchStories()

        val actualStories = storyViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStories)

        Assert.assertEquals(0, differ.snapshot().size)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}