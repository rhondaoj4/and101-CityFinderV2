package com.example.city_finder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivityDetailsBinding
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private var isDay: Boolean = true // Variable to hold the day/night state

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val city = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_CITY", City::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<City>("EXTRA_CITY")
        }

        val timeString24hr = intent.getStringExtra("EXTRA_TIME")

        if (city == null || timeString24hr == null) {
            handleError("Error: City or time data is missing.")
            return
        }

        binding.cityNameDetails.text = "${city.name}, ${city.country}"
        updateUiWithTime(timeString24hr)
        setupClickListeners(city)
        loadRandomCityImage(city)
    }

    private fun loadRandomCityImage(city: City) {
        lifecycleScope.launch {
            try {
                val response = UnsplashRetrofitInstance.api.getRandomPhoto(
                    clientId = ApiKeys.UNSPLASH_API,
                    query = "${city.name} travel"
                )
                if (response.isSuccessful && response.body() != null) {
                    val imageUrl = response.body()!!.urls.regular
                    binding.randomCityImage.load(imageUrl) {
                        crossfade(true)
                    }
                } else {
                    Log.e("DetailsActivity", "Failed to load random image: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DetailsActivity", "Error loading random image", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUiWithTime(timeString24hr: String) {
        try {
            val trimmedTimeString = timeString24hr.substringBefore(".")
            val timeFormatter24 = DateTimeFormatter.ofPattern("HH:mm:ss")
            val localTime = LocalTime.parse(trimmedTimeString, timeFormatter24)
            val timeFormatter12 = DateTimeFormatter.ofPattern("h:mm a")
            binding.timeDisplay.text = localTime.format(timeFormatter12)
            updateBackground(localTime)
        } catch (e: DateTimeParseException) {
            handleError("Could not parse time: $timeString24hr")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateBackground(time: LocalTime) {
        val hour = time.hour
        isDay = hour in 6..17 // Set our class-level variable

        val backgroundColor = if (isDay) {
            ContextCompat.getColor(this, R.color.light_blue)
        } else {
            ContextCompat.getColor(this, R.color.night_blue)
        }
        binding.detailsContainer.setBackgroundColor(backgroundColor)
    }

    private fun handleError(message: String) {
        Log.e("DetailsActivity", message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun setupClickListeners(city: City) {
        binding.btnWeather.setOnClickListener {
            val intent = Intent(this, WeatherActivity::class.java).apply {
                putExtra("EXTRA_CITY_NAME", city.name)
                putExtra("EXTRA_LAT", city.latitude)
                putExtra("EXTRA_LON", city.longitude)
                putExtra("IS_DAY_TIME", isDay) // <-- Pass the day/night status
            }
            startActivity(intent)
        }

        binding.btnCuisine.setOnClickListener {
            val countryName = city.country

            // ✅ THIS IS THE NEW, COMPREHENSIVE TRANSLATOR
            val mealDbCuisine = when (countryName) {
                // Direct Mappings
                "United States" -> "American"
                "United Kingdom" -> "British"
                "China" -> "Chinese"
                "People's Republic of China" -> "Chinese"
                "France" -> "French"
                "Germany" -> "German"
                "Greece" -> "Greek"
                "India" -> "Indian"
                "Ireland" -> "Irish"
                "Italy" -> "Italian"
                "Japan" -> "Japanese"
                "Mexico" -> "Mexican"
                "Spain" -> "Spanish"
                "Thailand" -> "Thai"
                "Vietnam" -> "Vietnamese"
                "South Korea", "North Korea" -> "Korean"

                // African
                "Algeria", "Angola", "Cameroon", "Congo", "Egypt",
                "Ethiopia", "Ghana", "Kenya", "Morocco", "Nigeria",
                "Senegal", "South Africa", "Sudan", "Tanzania", "Tunisia" -> "African"

                // Asian (for countries without their own specific mapping)
                "Indonesia", "Malaysia", "Pakistan", "Philippines", "Singapore" -> "Asian"

                // Caribbean
                "Bahamas", "Barbados", "Cuba", "Dominican Republic",
                "Haiti", "Jamaica", "Puerto Rico", "Trinidad and Tobago" -> "Caribbean"

                // Eastern European
                "Belarus", "Bulgaria", "Czech Republic", "Hungary", "Poland",
                "Romania", "Russia", "Slovakia", "Ukraine" -> "Eastern European"

                // European (for Western European countries without a specific mapping)
                "Austria", "Belgium", "Netherlands", "Portugal", "Switzerland" -> "European"

                // Latin American
                "Argentina", "Bolivia", "Brazil", "Chile", "Colombia",
                "Ecuador", "Paraguay", "Peru", "Uruguay", "Venezuela" -> "Latin American"

                // Mediterranean (for relevant countries not already mapped)
                "Croatia", "Cyprus", "Israel", "Lebanon", "Turkey" -> "Mediterranean"

                // Middle Eastern
                "Iran", "Iraq", "Jordan", "Kuwait", "Oman",
                "Qatar", "Saudi Arabia", "Syria", "United Arab Emirates", "Yemen" -> "Middle Eastern"

                // Nordic
                "Denmark", "Finland", "Iceland", "Norway", "Sweden" -> "Nordic"

                // Fallback: If no match, try the country name directly
                else -> countryName
            }

            val intent = Intent(this, CuisineLoadingActivity::class.java).apply {
                putExtra("EXTRA_COUNTRY_NAME", mealDbCuisine)
            }
            startActivity(intent)
        }

        binding.btnExploreAnother.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnCurrency.setOnClickListener {
            val intent = Intent(this, CurrencySelectionActivity::class.java).apply {
                putExtra("EXTRA_CITY_NAME", city.name)
                putExtra("EXTRA_COUNTRY_CODE", city.countryCode)
            }
            startActivity(intent)
        }


        binding.btnDemographics.setOnClickListener {
            val intent = Intent(this, DemographicsActivity::class.java).apply {
                // The API uses the 2-letter country code (e.g., "US")
                putExtra("EXTRA_COUNTRY_CODE", city.countryCode)
                putExtra("EXTRA_COUNTRY_NAME", city.country)
            }
            startActivity(intent)
        }

        // In DetailsActivity.kt, inside setupClickListeners()
        binding.btnSpots.setOnClickListener {
            val intent = Intent(this, SpotsActivity::class.java).apply {
                putExtra("EXTRA_LAT", city.latitude)
                putExtra("EXTRA_LON", city.longitude)
            }
            startActivity(intent)
        }

        binding.btnPhrases.setOnClickListener {
            val intent = Intent(this, PhrasesActivity::class.java).apply {
                putExtra("EXTRA_COUNTRY_CODE", city.countryCode)
            }
            startActivity(intent)
        }
    }


}
