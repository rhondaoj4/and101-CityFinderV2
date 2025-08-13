package com.example.city_finder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivityCuisineBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CuisineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCuisineBinding
    // This will hold the smaller SpoonacularRecipeResult objects
    private val recipeResults = mutableListOf<SpoonacularRecipeResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuisineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cuisineType = intent.getStringExtra("EXTRA_COUNTRY_NAME")
        if (cuisineType == null) {
            Toast.makeText(this, "Cuisine not specified.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.backButtonCuisine.setOnClickListener { finish() }
        setupClickListeners()
        fetchRecipesAndThenImages(cuisineType)
    }

    private fun fetchRecipesAndThenImages(cuisine: String) {
        lifecycleScope.launch {
            binding.cuisineProgressBar.visibility = View.VISIBLE
            try {
                // API Call 1: Get up to 20 recipes for the given cuisine.
                val recipeResponse = SpoonacularRetrofitInstance.api.searchRecipesByCuisine(
                    apiKey = ApiKeys.SPOON_API,
                    cuisine = cuisine
                )

                if (recipeResponse.isSuccessful && !recipeResponse.body()?.results.isNullOrEmpty()) {
                    // Shuffle the results and take the first 4 to randomize them.
                    val randomFourRecipes = recipeResponse.body()!!.results.shuffled().take(4)

                    recipeResults.clear()
                    recipeResults.addAll(randomFourRecipes)

                    // API Call 2: Get Unsplash images for the 4 random recipes
                    val imageViews = listOf(binding.recipeImage1, binding.recipeImage2, binding.recipeImage3, binding.recipeImage4)
                    loadUnsplashImagesConcurrently(randomFourRecipes, imageViews)

                } else {

                    // ✅ THIS IS THE CHANGE
                    // Instead of a toast, launch the new DadJokeActivity
                    val intent = Intent(this@CuisineActivity, DadJokeActivity::class.java)
                    startActivity(intent)
                    // We finish this activity so the user can't go back to the empty image grid
                    finish()
                }

            } catch (e: Exception) {
                Log.e("CuisineActivity", "Error fetching data", e)
                Toast.makeText(this@CuisineActivity, "An error occurred.", Toast.LENGTH_LONG).show()
            } finally {
                binding.cuisineProgressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun loadUnsplashImagesConcurrently(recipes: List<SpoonacularRecipeResult>, imageViews: List<ImageView>) = coroutineScope {
        recipes.mapIndexed { index, recipe ->
            async {
                if (index < imageViews.size) {
                    try {
                        val unsplashResponse = UnsplashRetrofitInstance.api.getRandomPhoto(
                            clientId = ApiKeys.UNSPLASH_API,
                            query = recipe.title
                        )
                        if (unsplashResponse.isSuccessful && unsplashResponse.body() != null) {
                            val imageUrl = unsplashResponse.body()!!.urls.regular
                            imageViews[index].load(imageUrl) { crossfade(true) }
                        }
                    } catch (e: Exception) {
                        Log.e("CuisineActivity", "Error loading Unsplash image for ${recipe.title}", e)
                    }
                }
            }
        }.awaitAll()
    }

    private fun setupClickListeners() {
        binding.recipeImage1.setOnClickListener { openRecipeDetail(0) }
        binding.recipeImage2.setOnClickListener { openRecipeDetail(1) }
        binding.recipeImage3.setOnClickListener { openRecipeDetail(2) }
        binding.recipeImage4.setOnClickListener { openRecipeDetail(3) }
    }

    private fun openRecipeDetail(index: Int) {
        if (index < recipeResults.size) {
            val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                // Pass the recipe ID to the detail screen
                putExtra("EXTRA_RECIPE_ID", recipeResults[index].id)
            }
            startActivity(intent)
        }
    }
}