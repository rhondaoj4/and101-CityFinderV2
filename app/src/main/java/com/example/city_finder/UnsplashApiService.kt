package com.example.city_finder

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {
    // This is your existing search function, we'll leave it as is
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("client_id") clientId: String,
        @Query("per_page") perPage: Int = 1
    ): Response<UnsplashSearchResponse>

    // --- ADD THIS NEW FUNCTION ---
    // This function gets a single random photo matching a specific query
    @GET("photos/random")
    suspend fun getRandomPhoto(
        @Query("client_id") clientId: String,
        @Query("query") query: String,
        @Query("orientation") orientation: String = "landscape" // Ensures we get wide images
    ): Response<UnsplashPhoto> // Note: The response is a single photo, not a search list
}

object UnsplashRetrofitInstance {
    val api: UnsplashApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApiService::class.java)
    }
}
