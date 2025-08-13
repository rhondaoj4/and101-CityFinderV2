package com.example.city_finder

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

// --- ADD THIS NEW DATA CLASS for the time response ---
data class GeoDbTimeResponse(
    @SerializedName("data") val data: String
)

interface ApiService {
    @GET("v1/geo/cities")
    suspend fun findCities(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String,
        @Query("minPopulation") minPopulation: Int,
        @Query("maxPopulation") maxPopulation: Int?,
        @Query("namePrefix") namePrefix: String?,
        @Query("countryIds") countryIds: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("sort") sort: String = "-population"
    ): Response<GeoDbResponse>

    @GET("v1/geo/cities/{cityId}")
    suspend fun getCityDetails(
        @Path("cityId") cityId: Int,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String
    ): Response<CityDetailsResponse>

    // --- ADD THIS NEW FUNCTION to get the time ---
    @GET("v1/geo/cities/{cityId}/time")
    suspend fun getCityTime(
        @Path("cityId") cityId: Int,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String
    ): Response<GeoDbTimeResponse>
}


object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://wft-geo-db.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
