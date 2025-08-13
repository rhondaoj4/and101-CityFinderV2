// Create a new file named CurrencySelectionActivity.kt
package com.example.city_finder

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.city_finder.databinding.ActivityCurrencySelectionBinding
import java.util.Currency
import java.util.Locale

class CurrencySelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrencySelectionBinding
    private val countryCurrencyMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cityName = intent.getStringExtra("EXTRA_CITY_NAME")
        val targetCountryCode = intent.getStringExtra("EXTRA_COUNTRY_CODE")

        if (targetCountryCode == null) {
            Toast.makeText(this, "Destination country code missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.backButtonCurrencySelect.setOnClickListener { finish() }

        // Populate the spinner with a list of countries
        setupCountrySpinner()

        binding.confirmButton.setOnClickListener {
            val selectedCountryName = binding.countrySpinner.selectedItem.toString()
            val baseCurrencyCode = countryCurrencyMap[selectedCountryName]
            val targetCurrencyCode = getCurrencyCodeForCountry(targetCountryCode)

            if (baseCurrencyCode != null) {
                val intent = Intent(this, CurrencyDetailsActivity::class.java).apply {
                    putExtra("EXTRA_CITY_NAME", cityName)
                    putExtra("EXTRA_BASE_CURRENCY", baseCurrencyCode)
                    putExtra("EXTRA_TARGET_CURRENCY", targetCurrencyCode)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupCountrySpinner() {
        // Create a list of countries and their currency codes
        val countries = Locale.getISOCountries().map { code ->
            val locale = Locale("", code)
            val countryName = locale.displayCountry
            try {
                val currencyCode = Currency.getInstance(locale).currencyCode
                countryCurrencyMap[countryName] = currencyCode
                countryName
            } catch (e: Exception) {
                null // Skip countries with no valid currency
            }
        }.filterNotNull().sorted()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.countrySpinner.adapter = adapter

        // Set default selection to user's current country (e.g., United States)
        val defaultLocale = Locale.getDefault()
        val defaultCountry = defaultLocale.displayCountry
        val defaultPosition = countries.indexOf(defaultCountry)
        if (defaultPosition >= 0) {
            binding.countrySpinner.setSelection(defaultPosition)
        }
    }

    private fun getCurrencyCodeForCountry(countryCode: String): String {
        return try {
            Currency.getInstance(Locale("", countryCode)).currencyCode
        } catch (e: Exception) { "USD" }
    }
}