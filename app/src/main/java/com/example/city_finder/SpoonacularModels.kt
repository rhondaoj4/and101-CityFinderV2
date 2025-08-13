// In SpoonacularModels.kt
package com.example.city_finder

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// For the initial search results (no longer used by Cuisine, but we can keep it for now)
data class SpoonacularSearchResponse(
    @SerializedName("results") val results: List<SpoonacularRecipeResult>
)

data class SpoonacularRecipeResult(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)


// NEW: A wrapper for the /random endpoint response
data class SpoonacularRandomResponse(
    @SerializedName("recipes") val recipes: List<SpoonacularRecipeInfo>
)

// CHANGE: Added @Parcelize annotation and implemented Parcelable
@Parcelize
data class SpoonacularRecipeInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("image") val imageUrl: String?,
    @SerializedName("extendedIngredients") val ingredients: List<Ingredient>,
    @SerializedName("analyzedInstructions") val instructions: List<Instruction>
) : Parcelable

@Parcelize
data class Ingredient(
    @SerializedName("original") val original: String
) : Parcelable

@Parcelize
data class Instruction(
    @SerializedName("steps") val steps: List<InstructionStep>
) : Parcelable

@Parcelize
data class InstructionStep(
    @SerializedName("number") val number: Int,
    @SerializedName("step") val step: String
) : Parcelable