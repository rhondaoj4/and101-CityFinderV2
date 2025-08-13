// In DadJokeActivity.kt
package com.example.city_finder

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.load
import com.example.city_finder.databinding.ActivityDadJokeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DadJokeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDadJokeBinding

    // CHANGE: The third item in the Triple is now an Int (the drawable ID), not a String.
    private val dadJokes = listOf(
        Triple(
            "Coldplay hasn't released a new song in years.",
            "Then they make two new singles in one night.",
            R.drawable.obama
        ),
        Triple(
            "The owner of a men's department store kept hovering over me, so I asked him to leave me alone.",
            "He said, 'Fine, suit yourself'",
            R.drawable.chris
        ),
        Triple(
            "Today a girl said he recognized me from the vegetarian club.",
            "I'm sure I've never met her before...get it HERBIVORE",
            R.drawable.scared_dog
        ),
        Triple(
            "How do you know when your clock is still hungry?",
            "It goes back four seconds.",
            R.drawable.boo
        ),
        Triple(
            "You wanna hear a joke about pizza?",
            "Nevermind... it's too cheesy.",
            R.drawable.aura
        ),
        Triple(
            "What do you call a fly with no wings?",
            "A walk.",
            R.drawable.fell_for_it
        ),
        Triple(
            "Knock knock.\nWho's there?\nCargo.\nCargo who?",
            "No, owl go 'who', car go 'beep beep'!",
            R.drawable.head_out
        )
    )

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadJokeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonJoke.setOnClickListener { finish() }

        revealJoke()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun revealJoke() {
        val randomJoke = dadJokes.random()

        binding.jokePunchlineText.visibility = View.INVISIBLE
        binding.jokeGifView.visibility = View.INVISIBLE

        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .build()

        lifecycleScope.launch {
            // 1. Show setup
            binding.jokeSetupText.text = randomJoke.first

            // 2. Wait
            delay(4000)

            // 3. Reveal punchline
            binding.jokePunchlineText.text = randomJoke.second
            binding.jokePunchlineText.visibility = View.VISIBLE

            // 4. Wait again
            delay(2000)

            // 5. Reveal and load the local GIF
            binding.jokeGifView.visibility = View.VISIBLE
            // The .load() function automatically works with drawable IDs.
            binding.jokeGifView.load(randomJoke.third, imageLoader) {
                crossfade(true)
                error(android.R.drawable.ic_menu_report_image)
            }
        }
    }
}