// Create a new file named HereApiService.kt
package com.example.city_finder

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface HereApiService {
    // The "discover" endpoint is used for searching categories
    @GET("v1/discover")
    suspend fun discoverPlaces(
        @Query("apiKey") apiKey: String,
        @Query("in") circle: String, // e.g., "circle:40.7128,-74.0060;r=80000"
        @Query("q") query: String, // A simple query like "tourist attraction"
        @Query("limit") limit: Int = 10
    ): Response<HereResponse>

    // NEW: Function to look up all details for a single place by its ID
    @GET("v1/lookup")
    suspend fun lookupPlaceDetails(
        @Query("apiKey") apiKey: String,
        @Query("id") id: String
    ): Response<HerePlace>
}

object HereRetrofitInstance {
    val api: HereApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://discover.search.hereapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HereApiService::class.java)
    }
}