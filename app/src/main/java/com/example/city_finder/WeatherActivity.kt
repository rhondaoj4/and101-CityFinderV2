package com.example.city_finder

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivityWeatherBinding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding
    private var isDayTime: Boolean = true // Store day/night state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cityName = intent.getStringExtra("EXTRA_CITY_NAME")
        val latitude = intent.getDoubleExtra("EXTRA_LAT", 0.0)
        val longitude = intent.getDoubleExtra("EXTRA_LON", 0.0)
        isDayTime = intent.getBooleanExtra("IS_DAY_TIME", true) // Set the state

        if (cityName == null) {
            handleError("City name is missing.")
            return
        }

        binding.weatherCityName.text = "Weather in $cityName"
        binding.backButton.setOnClickListener { finish() }
        setupThemeColors() // Renamed to reflect its new purpose

        if (latitude != 0.0 && longitude != 0.0) {
            fetchWeatherData(latitude, longitude, cityName)
        } else {
            handleError("Invalid location data.")
        }
    }

    // This now only sets text and icon colors, not the background.
    private fun setupThemeColors() {
        val textColor: Int
        val iconTintColor: Int

        if (isDayTime) {
            textColor = ContextCompat.getColor(this, R.color.black)
            iconTintColor = ContextCompat.getColor(this, R.color.dark_blue)
            binding.weatherContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
        } else {
            textColor = ContextCompat.getColor(this, R.color.white)
            iconTintColor = ContextCompat.getColor(this, R.color.white)
            binding.weatherContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.night_blue))
        }

        // Apply colors to all relevant text views
        binding.weatherCityName.setTextColor(textColor)
        binding.weatherTemperature.setTextColor(textColor)
        binding.weatherDescription.setTextColor(textColor)
        binding.feelsLikeText.setTextColor(textColor)
        binding.highLowText.setTextColor(textColor)
        binding.humidityText.setTextColor(textColor)
        binding.backButton.setColorFilter(iconTintColor)
    }

    private fun fetchWeatherData(lat: Double, lon: Double, cityName: String) {
        lifecycleScope.launch {
            binding.weatherProgressBar.visibility = View.VISIBLE
            try {
                val response = WeatherRetrofitInstance.api.getCurrentWeather(
                    latitude = lat,
                    longitude = lon,
                    apiKey = ApiKeys.WEATHER_API
                )
                if (response.isSuccessful && response.body() != null) {
                    updateUI(response.body()!!, cityName)
                } else {
                    handleError("Failed to get weather: ${response.code()}")
                }
            } catch (e: Exception) {
                handleError("Error fetching weather: ${e.message}")
            } finally {
                binding.weatherProgressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(weatherData: WeatherResponse, cityName: String) {
        val mainWeather = weatherData.main
        binding.weatherTemperature.text = "${mainWeather.temp.roundToInt()}°F"

        // --- NEW: Populate detailed weather info ---
        binding.feelsLikeText.text = "Feels like: ${mainWeather.feelsLike.roundToInt()}°"
        binding.highLowText.text = "H: ${mainWeather.tempMax.roundToInt()}° L: ${mainWeather.tempMin.roundToInt()}°"
        binding.humidityText.text = "Humidity: ${mainWeather.humidity}%"

        if (weatherData.weather.isNotEmpty()) {
            val weatherDetails = weatherData.weather[0]
            binding.weatherDescription.text = weatherDetails.description.replaceFirstChar { it.uppercase() }

            val iconCode = weatherDetails.icon
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png"
            binding.weatherIcon.load(iconUrl)

            loadWeatherImage(cityName, weatherDetails.description)

            // --- NEW: Start the background animation ---
            startWeatherAnimation(weatherDetails.main)
        }
    }

    // --- NEW: This function selects and starts the correct animation ---
    private fun startWeatherAnimation(weatherType: String) {
        // Reset visibility of all animation elements first
        binding.sunIcon.visibility = View.GONE
        binding.cloud1.visibility = View.GONE
        binding.cloud2.visibility = View.GONE

        when (weatherType) {
            "Clear" -> {
                if (isDayTime) {
                    binding.sunIcon.visibility = View.VISIBLE
                    animateSun()
                } else {
                    // You could add a star animation here for clear nights
                }
            }
            "Clouds" -> {
                binding.cloud1.visibility = View.VISIBLE
                binding.cloud2.visibility = View.VISIBLE
                animateDriftingObject(binding.cloud1, 20000L) // Slower drift
                animateDriftingObject(binding.cloud2, 30000L) // Even slower
            }
            "Rain", "Drizzle", "Thunderstorm" -> {
                // Here you would start a rain particle effect
                // For a simple version, you could animate ImageViews of raindrops
            }
            "Snow" -> {
                // Here you would start a snow particle effect
            }
        }
    }

    // --- NEW: Reusable animation for drifting clouds ---
    private fun animateDriftingObject(view: View, duration: Long) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        view.translationX = -view.width.toFloat() // Start off-screen left

        val animator = ObjectAnimator.ofFloat(view, "translationX", -view.width.toFloat(), screenWidth).apply {
            this.interpolator = LinearInterpolator()
            this.duration = duration
            this.repeatCount = ValueAnimator.INFINITE
        }
        animator.start()
    }

    // --- NEW: Reusable animation for a rotating sun ---
    private fun animateSun() {
        val animator = ObjectAnimator.ofFloat(binding.sunIcon, "rotation", 0f, 360f).apply {
            this.interpolator = LinearInterpolator()
            this.duration = 20000 // A slow 20-second rotation
            this.repeatCount = ValueAnimator.INFINITE
        }
        animator.start()
    }

    private fun loadWeatherImage(city: String, weather: String) {
        lifecycleScope.launch {
            try {
                val query = "$city $weather"
                val response = UnsplashRetrofitInstance.api.getRandomPhoto(
                    clientId = ApiKeys.UNSPLASH_API,
                    query = query
                )
                if (response.isSuccessful && response.body() != null) {
                    binding.weatherUnsplashImage.load(response.body()!!.urls.regular) { crossfade(true) }
                } else {
                    Log.e("WeatherActivity", "Failed to get Unsplash image: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("WeatherActivity", "Error loading Unsplash image", e)
            }
        }
    }

    private fun handleError(message: String) {
        Log.e("WeatherActivity", message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}