package com.example.city_finder

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.city_finder.databinding.ActivityRecipeDetailBinding
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonRecipe.setOnClickListener { finish() }

        val recipeId = intent.getIntExtra("EXTRA_RECIPE_ID", -1)
        if (recipeId == -1) {
            Toast.makeText(this, "Recipe ID missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch the specific details for the chosen recipe ID
        fetchRecipeDetails(recipeId)
    }

    private fun fetchRecipeDetails(id: Int) {
        lifecycleScope.launch {
            binding.recipeDetailProgress.visibility = View.VISIBLE
            try {
                val response = SpoonacularRetrofitInstance.api.getRecipeInformation(
                    id = id,
                    apiKey = ApiKeys.SPOON_API
                )

                if (response.isSuccessful && response.body() != null) {
                    updateUi(response.body()!!)
                } else {
                    Toast.makeText(this@RecipeDetailActivity, "Could not load recipe details.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("RecipeDetailActivity", "Error fetching details", e)
                Toast.makeText(this@RecipeDetailActivity, "An error occurred.", Toast.LENGTH_LONG).show()
            } finally {
                binding.recipeDetailProgress.visibility = View.GONE
            }
        }
    }

    private fun updateUi(recipe: SpoonacularRecipeInfo) {
        binding.detailRecipeTitle.text = recipe.title
        binding.detailRecipeImage.load(recipe.imageUrl) {
            crossfade(true)
        }

        val ingredientsText = recipe.ingredients.joinToString(separator = "\n") { "- ${it.original}" }
        binding.ingredientsList.text = ingredientsText

        if (recipe.instructions.isNotEmpty() && recipe.instructions[0].steps.isNotEmpty()) {
            val instructionsText = recipe.instructions[0].steps.joinToString(separator = "\n") { "${it.number}. ${it.step}" }
            binding.instructionsList.text = instructionsText
        } else {
            binding.instructionsList.text = "No instructions provided."
        }
    }
}