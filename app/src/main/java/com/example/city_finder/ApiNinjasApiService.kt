// Create a new file named ApiNinjasApiService.kt
package com.example.city_finder

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiNinjasApiService {
    @GET("v1/country")
    suspend fun getCountryData(
        @Header("X-Api-Key") apiKey: String,
        @Query("name") countryCode: String
    ): Response<List<CountryData>> // API returns a list with one item
}

object ApiNinjasRetrofitInstance {
    val api: ApiNinjasApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiNinjasApiService::class.java)
    }
}