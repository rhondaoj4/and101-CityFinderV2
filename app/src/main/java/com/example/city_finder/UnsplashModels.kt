package com.example.city_finder

import com.google.gson.annotations.SerializedName

data class UnsplashSearchResponse(
    @SerializedName("results") val results: List<UnsplashPhoto>
)

//single photo object
data class UnsplashPhoto(
    @SerializedName("urls") val urls: UnsplashPhotoUrls
)

data class UnsplashPhotoUrls(
    @SerializedName("regular") val regular: String
)