package com.example.city_finder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val onboardingSlides = listOf(
        "Welcome to the..." to "City Finder App",
        "Whether you want to find your next travel destination" to "travel",
        "a new place to call home" to "car on lonely road",
        "or you simply want to get obsessed with a picturesque city, we've got you" to "picturesque"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            startOnboarding()
        }
    }

    private suspend fun startOnboarding() {
        animateWelcomeText()
        delay(5000) // Increased delay to allow animation to finish

        for (i in 1 until onboardingSlides.size) {
            val (displayText, imageQuery) = onboardingSlides[i]
            updateSlide(displayText, imageQuery)
            delay(4000)
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // FIX: Updated to create the "staircase" effect
    private suspend fun animateWelcomeText() {
        binding.onboardingText.text = ""
        binding.onboardingText.alpha = 1f
        // Use a more reliable query to ensure the first image loads
        loadImage("beautiful landscape")

        val part1 = "Welcome to the\n"
        part1.forEach { char ->
            binding.onboardingText.append(char.toString())
            delay(50)
        }
        delay(500)

        // Added spaces before "Finder" and "App" to create the staircase
        val part2 = "\nCity\nFinder\nApp"
        part2.forEach { char ->
            binding.onboardingText.append(char.toString())
            delay(50)
        }
    }

    private suspend fun updateSlide(text: String, imageQuery: String) {
        fadeOutText()
        delay(500)
        binding.onboardingText.text = text
        loadImage(imageQuery)
        fadeInText()
    }

    // FIX: Added more detailed logging to help debug
    private suspend fun loadImage(query: String) {
        Log.d("OnboardingActivity", "Attempting to load image for query: '$query'")
        try {
            val response = UnsplashRetrofitInstance.api.getRandomPhoto(
                clientId = ApiKeys.UNSPLASH_API,
                query = query
            )
            if (response.isSuccessful && response.body() != null) {
                val imageUrl = response.body()!!.urls.regular
                Log.d("OnboardingActivity", "Successfully got image URL: $imageUrl")
                withContext(Dispatchers.Main) {
                    binding.backgroundImage.load(imageUrl) {
                        crossfade(750) // Smoother crossfade
                        error(android.R.drawable.ic_menu_report_image) // A more descriptive error icon
                    }
                }
            } else {
                // This log is crucial for debugging!
                Log.e("OnboardingActivity", "Failed to get image. Code: ${response.code()}, Message: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("OnboardingActivity", "Exception while loading image for query '$query'", e)
        }
    }

    private fun fadeInText() {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 500
        fadeIn.fillAfter = true
        binding.onboardingText.startAnimation(fadeIn)
    }

    private fun fadeOutText() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 500
        fadeOut.fillAfter = true
        binding.onboardingText.startAnimation(fadeOut)
    }
}
