// Create a new file named ApiNinjasModels.kt
package com.example.city_finder

import com.google.gson.annotations.SerializedName

// The API returns a list, so we'll expect a List<CountryData>
data class CountryData(
    @SerializedName("sex_ratio") val sexRatio: Double?,
    @SerializedName("unemployment") val unemployment: Double?,
    @SerializedName("homicide_rate") val homicideRate: Double?,
    @SerializedName("gdp") val gdp: Double?,
    @SerializedName("gdp_per_capita") val gdpPerCapita: Double?,
    @SerializedName("population") val population: Long?,
    @SerializedName("pop_density") val popDensity: Double?,
    @SerializedName("tourists") val tourists: Double?,
    @SerializedName("post_secondary_enrollment_female") val postSecondaryEnrollmentFemale: Double?,
    @SerializedName("post_secondary_enrollment_male") val postSecondaryEnrollmentMale: Double?
)