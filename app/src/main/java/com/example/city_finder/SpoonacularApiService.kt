package com.example.city_finder

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularApiService {

    // Use the reliable complexSearch endpoint to get a pool of recipes
    @GET("recipes/complexSearch")
    suspend fun searchRecipesByCuisine(
        @Query("apiKey") apiKey: String,
        @Query("cuisine") cuisine: String,
        @Query("number") number: Int = 20 // Get 20 to have a good list to randomize
    ): Response<SpoonacularSearchResponse>

    // This is used by the detail screen to get full recipe info
    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String,
        @Query("includeNutrition") includeNutrition: Boolean = false
    ): Response<SpoonacularRecipeInfo>
}

object SpoonacularRetrofitInstance {
    val api: SpoonacularApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpoonacularApiService::class.java)
    }
}