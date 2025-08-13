// Create a new file named HereModels.kt
package com.example.city_finder

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class HereResponse(
    @SerializedName("items") val items: List<HerePlace>
)

@Parcelize
data class HerePlace(
    @SerializedName("title") val title: String,
    @SerializedName("address") val address: HereAddress,
    @SerializedName("id") val id: String,
    // NEW: Add fields that come from the /lookup endpoint
    @SerializedName("categories") val categories: List<HereCategory>?,
    @SerializedName("contacts") val contacts: List<HereContacts>?,
    @SerializedName("openingHours") val openingHours: List<HereOpeningHours>?
) : Parcelable

@Parcelize
data class HereAddress(
    @SerializedName("label") val label: String
) : Parcelable

// NEW: More detailed classes for the lookup response
@Parcelize
data class HereCategory(
    @SerializedName("name") val name: String
) : Parcelable

@Parcelize
data class HereContacts(
    @SerializedName("phone") val phone: List<HereContactItem>?,
    @SerializedName("www") val www: List<HereContactItem>? // Website
) : Parcelable

@Parcelize
data class HereContactItem(
    @SerializedName("value") val value: String
) : Parcelable

@Parcelize
data class HereOpeningHours(
    @SerializedName("text") val text: List<String>?
) : Parcelable
