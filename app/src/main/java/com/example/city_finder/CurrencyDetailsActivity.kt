// In CurrencyDetailsActivity.kt
package com.example.city_finder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivityCurrencyDetailsBinding
import kotlinx.coroutines.launch

class CurrencyDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrencyDetailsBinding
    private var exchangeRate: Double = 0.0
    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cityName = intent.getStringExtra("EXTRA_CITY_NAME")
        val baseCurrency = intent.getStringExtra("EXTRA_BASE_CURRENCY")
        val targetCurrency = intent.getStringExtra("EXTRA_TARGET_CURRENCY")

        if (baseCurrency == null || targetCurrency == null) {
            Toast.makeText(this, "Currency codes missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.currencyTitle.text = "Currency in $cityName"
        binding.backButtonCurrency.setOnClickListener { finish() }
        binding.homeCurrencyLayout.hint = baseCurrency
        binding.localCurrencyLayout.hint = targetCurrency

        fetchPairConversion(baseCurrency, targetCurrency)
        setupTextWatchers()
    }

    private fun fetchPairConversion(base: String, target: String) {
        lifecycleScope.launch {
            binding.currencyProgressBar.visibility = View.VISIBLE
            try {
                val response = CurrencyRetrofitInstance.api.getPairConversion(
                    apiKey = ApiKeys.CURRENCY_API, // Make sure this matches your ApiKeys.kt
                    baseCurrency = base,
                    targetCurrency = target
                )
                if (response.isSuccessful && response.body() != null) {
                    exchangeRate = response.body()!!.conversionRate
                    binding.exchangeRateText.text = "1 $base = ${"%.4f".format(exchangeRate)} $target"
                    loadCurrencyImage(target)
                } else {
                    Toast.makeText(this@CurrencyDetailsActivity, "Could not fetch exchange rate.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CurrencyDetailsActivity", "Error fetching data", e)
                Toast.makeText(this@CurrencyDetailsActivity, "An error occurred.", Toast.LENGTH_LONG).show()
            } finally {
                binding.currencyProgressBar.visibility = View.GONE
            }
        }
    }

    private fun loadCurrencyImage(currencyCode: String) {
        lifecycleScope.launch {
            try {
                val imageResponse = UnsplashRetrofitInstance.api.getRandomPhoto(
                    clientId = ApiKeys.UNSPLASH_API,
                    query = "$currencyCode money banknote"
                )
                if (imageResponse.isSuccessful && imageResponse.body() != null) {
                    binding.currencyImage.load(imageResponse.body()!!.urls.regular) { crossfade(true) }
                }
            } catch (e: Exception) {
                Log.e("CurrencyDetailsActivity", "Error fetching image", e)
            }
        }
    }

    private fun setupTextWatchers() {
        // This function's logic remains the same as the previous version
        binding.homeCurrencyInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                if (s.toString().isNotEmpty()) {
                    val homeAmount = s.toString().toDoubleOrNull() ?: 0.0
                    val localAmount = homeAmount * exchangeRate
                    binding.localCurrencyInput.setText("%.2f".format(localAmount))
                } else {
                    binding.localCurrencyInput.text?.clear()
                }
                isUpdating = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.localCurrencyInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                if (s.toString().isNotEmpty() && exchangeRate > 0) {
                    val localAmount = s.toString().toDoubleOrNull() ?: 0.0
                    val homeAmount = localAmount / exchangeRate
                    binding.homeCurrencyInput.setText("%.2f".format(homeAmount))
                } else {
                    binding.homeCurrencyInput.text?.clear()
                }
                isUpdating = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}