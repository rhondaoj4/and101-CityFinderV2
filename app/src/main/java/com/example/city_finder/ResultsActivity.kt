package com.example.city_finder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.city_finder.databinding.ActivityResultsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private lateinit var cityGridAdapter: CityGridAdapter

    private val apiHost = "wft-geo-db.p.rapidapi.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        val minPop = intent.getIntExtra("MIN_POP", 0)
        val maxPop = intent.getIntExtra("MAX_POP", -1).takeIf { it != -1 }
        val prefix = intent.getStringExtra("PREFIX")
        val kCities = intent.getIntExtra("K_CITIES", 1)
        val countryIds = intent.getStringExtra("COUNTRY_IDS")

        lifecycleScope.launch {
            binding.resultsProgressBar.visibility = View.VISIBLE
            findAndDisplayRandomCities(minPop, maxPop, prefix, countryIds, kCities)
            binding.resultsProgressBar.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        cityGridAdapter = CityGridAdapter(emptyList()) { city ->
            val intent = Intent(this, ExploreActivity::class.java)
            intent.putExtra("EXTRA_CITY", city)
            startActivity(intent)
        }

        binding.resultsRecyclerView.apply {
            adapter = cityGridAdapter
            layoutManager = GridLayoutManager(this@ResultsActivity, 2)
        }
    }

    private suspend fun findAndDisplayRandomCities(minPop: Int, maxPop: Int?, prefix: String?, countryIds: String?, k: Int) {
        try {
            val countResponse = RetrofitInstance.api.findCities(
                apiKey = ApiKeys.GEO_API,
                apiHost = apiHost,
                minPopulation = minPop,
                maxPopulation = maxPop,
                namePrefix = prefix,
                countryIds = countryIds,
                limit = 1,
                offset = 0
            )

            if (countResponse.isSuccessful && countResponse.body() != null) {
                val totalCount = countResponse.body()!!.metadata.totalCount
                Log.d("ResultsActivity", "Total cities found: $totalCount")

                if (totalCount == 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ResultsActivity, "No cities found.", Toast.LENGTH_SHORT).show()
                        cityGridAdapter.updateData(emptyList())
                    }
                    return
                }

                delay(1500)

                val limit = minOf(k, totalCount)
                val maxOffset = totalCount - limit
                val randomOffset = if (maxOffset > 0) Random.nextInt(0, maxOffset) else 0

                val cityResponse = RetrofitInstance.api.findCities(
                    apiKey = ApiKeys.GEO_API,
                    apiHost = apiHost,
                    minPopulation = minPop,
                    maxPopulation = maxPop,
                    namePrefix = prefix,
                    countryIds = countryIds,
                    limit = limit,
                    offset = randomOffset
                )

                if (cityResponse.isSuccessful && cityResponse.body() != null) {
                    val cities = cityResponse.body()!!.data
                    val citiesWithImages = fetchImagesForCities(cities)
                    withContext(Dispatchers.Main) {
                        cityGridAdapter.updateData(citiesWithImages)
                    }
                } else {
                    handleApiError("Failed to fetch cities. Code: ${cityResponse.code()}")
                }
            } else {
                handleApiError("Failed to get count. Code: ${countResponse.code()}")
            }
        } catch (e: Exception) {
            Log.e("ResultsActivity", "API call failed", e)
            handleApiError("An error occurred: ${e.message}")
        }
    }

    private suspend fun fetchImagesForCities(cities: List<City>): List<City> = coroutineScope {
        cities.map { city ->
            async {
                try {
                    val searchResponse = UnsplashRetrofitInstance.api.searchPhotos(
                        query = city.name,
                        clientId = ApiKeys.UNSPLASH_API
                    )
                    if (searchResponse.isSuccessful && searchResponse.body()?.results?.isNotEmpty() == true) {
                        val imageUrl = searchResponse.body()!!.results[0].urls.regular
                        city.imageUrl = imageUrl
                    }
                } catch (e: Exception) {
                    Log.e("ResultsActivity", "Unsplash API call failed for city ${city.name}", e)
                }
                city
            }
        }.awaitAll()
    }

    private suspend fun handleApiError(message: String) {
        withContext(Dispatchers.Main) {
            Log.e("ResultsActivity", message)
            Toast.makeText(this@ResultsActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}
