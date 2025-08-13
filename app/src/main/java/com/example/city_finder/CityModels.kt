package com.example.city_finder

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GeoDbResponse(
    @SerializedName("data") val data: List<City>,
    @SerializedName("metadata") val metadata: Metadata
)

data class CityDetailsResponse(
    @SerializedName("data") val data: City
)

@Parcelize
data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("countryCode") val countryCode: String, // <-- ADD THIS LINE
    @SerializedName("population") val population: Long,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") var timezone: String? = null,
    var imageUrl: String? = null
) : Parcelable


data class Metadata(
    @SerializedName("totalCount") val totalCount: Int
)
