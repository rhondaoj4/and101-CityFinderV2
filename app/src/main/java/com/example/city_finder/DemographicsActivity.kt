// Create a new file named DemographicsActivity.kt
package com.example.city_finder

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Path
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.city_finder.databinding.ActivityDemographicsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import android.animation.Animator
import java.util.Locale
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit


class DemographicsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemographicsBinding
    private var backgroundSoundPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemographicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val countryCode = intent.getStringExtra("EXTRA_COUNTRY_CODE")
        val countryName = intent.getStringExtra("EXTRA_COUNTRY_NAME")

        if (countryCode == null) {
            Toast.makeText(this, "Country code missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.backButtonDemographics.setOnClickListener { finish() }

        // NEW: Start the firework sound and make it loop
        backgroundSoundPlayer = MediaPlayer.create(this, R.raw.firework_sound)
        backgroundSoundPlayer?.isLooping = true
        backgroundSoundPlayer?.start()

        binding.demographicsSubtitle.text = "Demographics for $countryName"

        fetchCountryData(countryCode)
    }

    private fun fetchCountryData(code: String) {
        lifecycleScope.launch {
            binding.demographicsProgressBar.visibility = View.VISIBLE
            try {
                val response = ApiNinjasRetrofitInstance.api.getCountryData(ApiKeys.NINJA_API, code)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val data = response.body()!![0]
                    // Once data is fetched, start the animation sequence
                    startStatAnimations(data)
                } else {
                    Toast.makeText(this@DemographicsActivity, "Could not load demographics.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DemographicsActivity", "Error fetching data", e)
                Toast.makeText(this@DemographicsActivity, "An error occurred.", Toast.LENGTH_LONG).show()
            } finally {
                binding.demographicsProgressBar.visibility = View.GONE
            }
        }
    }

    private fun startStatAnimations(data: CountryData) {
        lifecycleScope.launch {
            val formatter = NumberFormat.getInstance(Locale.US)

            // Define the stats, their values, and a placeholder icon
            val stats = listOf(
                "Sex Ratio (M/F)" to data.sexRatio?.toString(),
                "Unemployment" to "${data.unemployment}%",
                "Homicide Rate" to data.homicideRate?.toString(),
                "GDP" to "$${formatter.format(data.gdp)}",
                "GDP per Capita" to "$${formatter.format(data.gdpPerCapita)}",
                "Population" to formatter.format(data.population),
                "Tourists" to formatter.format(data.tourists),
                "Female Post-Secondary" to "${data.postSecondaryEnrollmentFemale}%",
                "Male Post-Secondary" to "${data.postSecondaryEnrollmentMale}%"
            )

            // Launch each stat as a "shooting star" with a delay between each
            for ((label, value) in stats) {
                if (value != null) {
                    // This is a placeholder icon, you can create a map of labels to real icons later
                    val iconResId = android.R.drawable.star_on
                    launchStatAnimation(label, value, iconResId)
                    delay(1500) // Wait 1.5 seconds before launching the next one
                }
            }
        }
    }

    private fun launchStatAnimation(label: String, value: String, iconResId: Int) {
        val container = binding.demographicsContainer
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // --- THIS IS THE MISSING CODE THAT NEEDS TO BE ADDED BACK ---

        // Create the layout for the "star"
        val starLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val icon = ImageView(this@DemographicsActivity).apply {
                setImageResource(iconResId)
                layoutParams = FrameLayout.LayoutParams(100, 100) // 100x100 pixels
            }

            val text = TextView(this@DemographicsActivity).apply {
                this.text = "$label: $value"
                textSize = 20f
                setTextColor(Color.WHITE)
                setShadowLayer(10f, 0f, 0f, Color.BLACK)
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER_VERTICAL or Gravity.END
                    marginStart = 110 // Position text to the right of the icon
                }
            }

            addView(icon)
            addView(text)
        }

        container.addView(starLayout)

        // Create a curved path for the animation
        val path = Path().apply {
            moveTo(-400f, screenHeight * 0.2f) // Start off-screen left
            quadTo(screenWidth / 2, screenHeight, screenWidth + 400f, screenHeight * 0.3f) // Arc across screen and off to the right
        }

        val animator = ObjectAnimator.ofFloat(starLayout, View.X, View.Y, path).apply {
            duration = 4500 // 4.5-second animation
        }
        animator.start()

        // NEW: Trigger a colorful firework burst immediately
        triggerFireworkBurst()

        // Clean up the star view after its animation is done
        starLayout.postDelayed({
            container.removeView(starLayout)
        }, 4500) // Matches the animation duration
    }

    // NEW: Rewritten function to create a Konfetti firework burst
    private fun triggerFireworkBurst() {
        binding.konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def), // Red, green, blue, etc.
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )
        )
    }

    override fun onStop() {
        super.onStop()
        // Clean up the media player when the activity is no longer visible
        backgroundSoundPlayer?.release()
        backgroundSoundPlayer = null
    }
}


