package com.example.city_finder

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.city_finder.databinding.ActivityLoadingBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding

    private val cuteSayings = listOf(
        "Traveling to a place that needs you in it...",
        "Just packing the good vibes...",
        "Plotting your next great escape!",
        "Don't forget your imaginary passport!",
        "Finding a city as awesome as you are.",
        "Our travel-o-meter is spinning!",
        "Warming up the teleportation device...",
        "Searching for your soul-city.",
        "Hold on, consulting the globes.",
        "Get ready for a new obsession."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loadingText.text = cuteSayings.random()

        // Start the plane animation as soon as the screen loads
        startPlaneAnimation()

        val minPop = intent.getIntExtra("MIN_POP", 0)
        val maxPop = intent.getIntExtra("MAX_POP", -1)
        val prefix = intent.getStringExtra("PREFIX")
        val kCities = intent.getIntExtra("K_CITIES", 1)
        val countryIds = intent.getStringExtra("COUNTRY_IDS")

        lifecycleScope.launch {
            // Delay to allow the animation to play
            delay(3000)

            val intent = Intent(this@LoadingActivity, ResultsActivity::class.java).apply {
                putExtra("MIN_POP", minPop)
                putExtra("MAX_POP", if(maxPop == -1) null else maxPop)
                putExtra("PREFIX", prefix)
                putExtra("K_CITIES", kCities)
                putExtra("COUNTRY_IDS", countryIds)
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Creates and starts the animation to make the plane fly across the screen.
     */
    private fun startPlaneAnimation() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toFloat()

        // This animator moves the plane from off-screen left to off-screen right
        val animator = ObjectAnimator.ofFloat(
            binding.planeIcon,
            "translationX",
            -200f, // Start position
            screenWidth // End position
        ).apply {
            duration = 3000 // Animation lasts 3 seconds
            interpolator = AccelerateDecelerateInterpolator() // Smooth motion
        }
        animator.start()
    }
}
