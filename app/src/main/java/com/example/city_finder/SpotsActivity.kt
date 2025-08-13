// In SpotsActivity.kt
package com.example.city_finder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivitySpotsBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

class SpotsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpotsBinding
    private val spots = mutableListOf<HerePlace>()
    // HERE uses simple query strings instead of complex IDs
    private val queries = listOf("tourist attraction", "museum", "park", "restaurant")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lat = intent.getDoubleExtra("EXTRA_LAT", 0.0)
        val lon = intent.getDoubleExtra("EXTRA_LON", 0.0)

        binding.backButtonSpots.setOnClickListener { finish() }
        setupClickListeners()

        fetchSpots(lat, lon)
    }

    private fun fetchSpots(lat: Double, lon: Double) {
        lifecycleScope.launch {
            binding.spotsProgressBar.visibility = View.VISIBLE
            try {
                // Fetch spots from all queries in parallel
                val allPlaces = fetchSpotsFromMultipleQueries(lat, lon)

                if (allPlaces.isNotEmpty()) {
                    val randomFourSpots = allPlaces.distinctBy { it.id }.shuffled().take(4)

                    spots.clear()
                    spots.addAll(randomFourSpots)

                    val imageViews = listOf(binding.spotImage1, binding.spotImage2, binding.spotImage3, binding.spotImage4)
                    loadSpotImages(randomFourSpots, imageViews)

                } else {
                    val intent = Intent(this@SpotsActivity, DadJokeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("SpotsActivity", "Error fetching spots", e)
                Toast.makeText(this@SpotsActivity, "An error occurred.", Toast.LENGTH_LONG).show()
            } finally {
                binding.spotsProgressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun fetchSpotsFromMultipleQueries(lat: Double, lon: Double): List<HerePlace> = coroutineScope {
        val circleArea = String.format(Locale.US, "circle:%.4f,%.4f;r=%d", lat, lon, 50000) // 50km radius

        val deferredResults = queries.map { query ->
            async {
                try {
                    val response = HereRetrofitInstance.api.discoverPlaces(
                        apiKey = ApiKeys.HERE_API,
                        circle = circleArea,
                        query = query,
                        limit = 10
                    )
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.items
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("SpotsActivity", "API call failed for query $query", e)
                    emptyList<HerePlace>()
                }
            }
        }
        deferredResults.awaitAll().flatten()
    }

    private suspend fun loadSpotImages(places: List<HerePlace>, imageViews: List<ImageView>) = coroutineScope {
        places.mapIndexed { index, place ->
            async {
                if (index < imageViews.size) {
                    // HERE API doesn't provide reliable images, so we'll always use Unsplash
                    try {
                        val unsplashResponse = UnsplashRetrofitInstance.api.getRandomPhoto(
                            clientId = ApiKeys.UNSPLASH_API,
                            query = place.title
                        )
                        if (unsplashResponse.isSuccessful && unsplashResponse.body() != null) {
                            val imageUrl = unsplashResponse.body()!!.urls.regular
                            imageViews[index].load(imageUrl) { crossfade(true) }
                        }
                    } catch (e: Exception) {
                        Log.e("SpotsActivity", "Unsplash fallback failed", e)
                    }
                }
            }
        }.awaitAll()
    }

    private fun setupClickListeners() {
        binding.spotImage1.setOnClickListener { openSpotDetail(0) }
        binding.spotImage2.setOnClickListener { openSpotDetail(1) }
        binding.spotImage3.setOnClickListener { openSpotDetail(2) }
        binding.spotImage4.setOnClickListener { openSpotDetail(3) }
    }

    private fun openSpotDetail(index: Int) {
        if (index < spots.size) {
            val intent = Intent(this, SpotDetailActivity::class.java).apply {
                putExtra("EXTRA_SPOT_ID", spots[index].id)
            }
            startActivity(intent)
        }
    }
}