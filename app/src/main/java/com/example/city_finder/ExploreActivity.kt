package com.example.city_finder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.city_finder.databinding.ActivityExploreBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding
    private val apiHost = "wft-geo-db.p.rapidapi.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the city name initially for the suspenseful reveal
        binding.exploreCityName.visibility = View.INVISIBLE

        val city = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_CITY", City::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<City>("EXTRA_CITY")
        }

        if (city == null) {
            Toast.makeText(this, "Error: Could not load city data.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- FIX: This coroutine now handles both the animation AND the API calls ---
        lifecycleScope.launch {
            // 1. Animate the city name first, using the data we already have
            animateCityName(city)

            // 2. After the animation, fetch the details in the background
            fetchDetailsAndProceed(city)
        }
    }

    /**
     * Creates the suspenseful reveal of the city name.
     */
    private suspend fun animateCityName(city: City) {
        // Wait for 1.5 seconds for suspense
        delay(1500)

        // Set the text and make it visible with a fade-in animation
        binding.exploreCityName.text = "${city.name}, ${city.country}"
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1000 // 1 second fade-in
        binding.exploreCityName.startAnimation(fadeIn)
        binding.exploreCityName.visibility = View.VISIBLE

        // Wait another 2 seconds so the user has time to read the name
        delay(2000)
    }

    /**
     * Fetches full city details and time sequentially, then launches DetailsActivity.
     */
    private suspend fun fetchDetailsAndProceed(city: City) {
        try {
            // First, get the city details
            val detailsResponse = RetrofitInstance.api.getCityDetails(
                cityId = city.id,
                apiKey = ApiKeys.GEO_API,
                apiHost = apiHost
            )

            if (!detailsResponse.isSuccessful || detailsResponse.body() == null) {
                handleError("Failed to get city details: ${detailsResponse.code()}")
                return
            }

            // Wait to respect the rate limit
            delay(1500)

            // Then, get the city time
            val timeResponse = RetrofitInstance.api.getCityTime(
                cityId = city.id,
                apiKey = ApiKeys.GEO_API,
                apiHost = apiHost
            )

            if (timeResponse.isSuccessful && timeResponse.body() != null) {
                val detailedCity = detailsResponse.body()!!.data
                val timeString = timeResponse.body()!!.data

                val intent = Intent(this, DetailsActivity::class.java).apply {
                    putExtra("EXTRA_CITY", detailedCity)
                    putExtra("EXTRA_TIME", timeString)
                }
                startActivity(intent)
                finish()
            } else {
                handleError("Failed to get city time: ${timeResponse.code()}")
            }

        } catch (e: Exception) {
            handleError("Error fetching city info: ${e.message}")
        }
    }

    private fun handleError(message: String) {
        Log.e("ExploreActivity", message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
