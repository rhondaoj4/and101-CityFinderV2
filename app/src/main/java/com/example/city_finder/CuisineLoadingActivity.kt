// In CuisineLoadingActivity.kt
package com.example.city_finder

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.city_finder.databinding.ActivityCuisineLoadingBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CuisineLoadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCuisineLoadingBinding
    // CHANGE: Two MediaPlayers for two simultaneous sounds
    private var grillingPlayer: MediaPlayer? = null
    private var gasLightPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuisineLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val countryName = intent.getStringExtra("EXTRA_COUNTRY_NAME")

        // --- NEW: VIDEO PLAYER LOGIC ---
        // 1. Set the path for the video from your raw resources
        val videoPath = "android.resource://" + packageName + "/" + R.raw.wok_tossing
        val videoUri = Uri.parse(videoPath)
        binding.wokVideoView.setVideoURI(videoUri)

        // 2. Make the video loop continuously
        binding.wokVideoView.setOnPreparedListener { mp ->
            mp.isLooping = true
        }

        // 3. Start the video
        binding.wokVideoView.start()


        // --- NEW: AUDIO LOGIC ---
        // Play grilling sound and make it loop
        grillingPlayer = MediaPlayer.create(this, R.raw.grilling)
        grillingPlayer?.isLooping = true
        grillingPlayer?.start()


        lifecycleScope.launch {
            delay(4000) // Let video and sound play for 4 seconds
            val intent = Intent(this@CuisineLoadingActivity, CuisineActivity::class.java).apply {
                putExtra("EXTRA_COUNTRY_NAME", countryName)
            }
            startActivity(intent)
            finish()
        }
    }

    // The old tossPanAnimation() and playSound() functions are no longer needed and can be deleted.

    override fun onStop() {
        super.onStop()
        // CHANGE: Clean up both media players to prevent memory leaks
        grillingPlayer?.release()
        gasLightPlayer?.release()
        grillingPlayer = null
        gasLightPlayer = null
    }
}