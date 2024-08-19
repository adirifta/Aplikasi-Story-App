package com.example.aplikasistoryapp

import com.example.aplikasistoryapp.data.response.ListStoryItem

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0 until 100) {
            val story = ListStoryItem(
                id = i.toString(),
                name = "name $i",
                description = "description $i",
                photoUrl = "photoUrl $i",
                createdAt = "2022-02-02T22:22:22Z",
                lat = -6.200000 + i,
                lon = 106.816666 + i
            )
            items.add(story)
        }
        return items
    }
}
