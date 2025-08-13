// In CurrencyModels.kt
package com.example.city_finder

import com.google.gson.annotations.SerializedName

// NEW: Data class for the /pair endpoint response
data class PairConversionResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("target_code") val targetCode: String,
    @SerializedName("conversion_rate") val conversionRate: Double
)