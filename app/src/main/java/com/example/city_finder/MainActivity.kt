package com.example.city_finder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.city_finder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val minPop = binding.minPopInput.text.toString().toIntOrNull()
            val maxPop = binding.maxPopInput.text.toString().toIntOrNull()
            val prefix = binding.cityPrefix.text.toString().ifBlank { null }
            val countryIds = binding.countryIdsInput.text.toString().ifBlank { null }

            // --- VALIDATION LOGIC ADDED HERE ---

            // 1. Validate Minimum Population
            if (minPop == null) {
                Toast.makeText(this, "Minimum population is required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validate Number of Cities
            val kCities = binding.kSuggestionsInput.text.toString().toIntOrNull()
            if (kCities == null) {
                Toast.makeText(this, "Please enter the number of cities.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (kCities !in 2..10) {
                Toast.makeText(this, "Number of cities must be between 2 and 10.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If all checks pass, create the Intent and start the next activity
            val intent = Intent(this, LoadingActivity::class.java).apply {
                putExtra("MIN_POP", minPop)
                maxPop?.let { putExtra("MAX_POP", it) }
                putExtra("PREFIX", prefix)
                putExtra("K_CITIES", kCities) // Pass the validated number
                putExtra("COUNTRY_IDS", countryIds)
            }
            startActivity(intent)
        }
    }
}
