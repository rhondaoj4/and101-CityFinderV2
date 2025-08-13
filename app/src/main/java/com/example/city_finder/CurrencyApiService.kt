// In CurrencyApiService.kt
package com.example.city_finder

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApiService {
    // NEW: Use the /pair endpoint for a direct conversion
    @GET("v6/{apiKey}/pair/{baseCurrency}/{targetCurrency}")
    suspend fun getPairConversion(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String,
        @Path("targetCurrency") targetCurrency: String
    ): Response<PairConversionResponse> // Note the new response type
}

object CurrencyRetrofitInstance {
    val api: CurrencyApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://v6.exchangerate-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApiService::class.java)
    }
}