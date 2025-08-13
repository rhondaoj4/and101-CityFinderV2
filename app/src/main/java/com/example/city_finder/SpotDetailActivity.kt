// In SpotDetailActivity.kt
package com.example.city_finder

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivitySpotDetailBinding
import kotlinx.coroutines.launch

class SpotDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpotDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpotDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonSpotDetail.setOnClickListener { finish() }

        val spotId = intent.getStringExtra("EXTRA_SPOT_ID")

        if (spotId == null) {
            Toast.makeText(this, "Spot ID missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Fetch the full details for this specific spot
        fetchSpotDetails(spotId)
    }

    private fun fetchSpotDetails(id: String) {
        lifecycleScope.launch {
            try {
                val response = HereRetrofitInstance.api.lookupPlaceDetails(
                    apiKey = ApiKeys.HERE_API,
                    id = id
                )

                if (response.isSuccessful && response.body() != null) {
                    updateUi(response.body()!!)
                } else {
                    Toast.makeText(this@SpotDetailActivity, "Could not load spot details.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("SpotDetailActivity", "Error fetching details", e)
            }
        }
    }

    private fun updateUi(spot: HerePlace) {
        binding.spotDetailName.text = spot.title
        binding.spotDetailAddress.text = spot.address.label

        // Get the category name
        val category = spot.categories?.firstOrNull()?.name ?: "Cool Spot"

        // Get the first available website
        val website = spot.contacts?.firstOrNull()?.www?.firstOrNull()?.value

        // Get the opening hours text
        val hours = spot.openingHours?.firstOrNull()?.text?.joinToString("\n")

        binding.spotDetailHours.text = hours ?: "Hours not available"
        binding.spotDetailHours.visibility = if (hours != null) View.VISIBLE else View.GONE

        // We can add a TextView for the website if we want, or log it
        Log.d("SpotDetailActivity", "Website: $website")

        // Hide rating bar as HERE API doesn't provide a simple rating
        binding.spotRatingBar.visibility = View.GONE

        // Load an image using the category and name as a query
        lifecycleScope.launch {
            try {
                val unsplashResponse = UnsplashRetrofitInstance.api.getRandomPhoto(
                    clientId = ApiKeys.UNSPLASH_API,
                    query = "${spot.title} $category"
                )
                if (unsplashResponse.isSuccessful && unsplashResponse.body() != null) {
                    binding.spotDetailImage.load(unsplashResponse.body()!!.urls.regular) { crossfade(true) }
                }
            } catch (e: Exception) {
                Log.e("SpotDetailActivity", "Image load failed", e)
            }
        }
    }
}