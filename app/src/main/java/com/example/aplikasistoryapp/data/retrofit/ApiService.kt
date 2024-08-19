package com.example.aplikasistoryapp.data.retrofit

import com.example.aplikasistoryapp.data.response.LoginResponse
import com.example.aplikasistoryapp.data.response.RegisterResponse
import com.example.aplikasistoryapp.data.response.StoryDetailResponse
import com.example.aplikasistoryapp.data.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getStories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): StoryResponse

    @GET("stories/{id}")
    suspend fun getStoryDetail(@Path("id") id: String): StoryDetailResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): StoryResponse

    @Multipart
    @POST("stories/guest")
    suspend fun addStoryGuest(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): StoryResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Query("location") location: Int = 1,
    ): StoryResponse
}