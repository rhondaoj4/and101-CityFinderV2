package com.example.city_finder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class CityGridAdapter(
    private var cities: List<City>,
    private val onItemClicked: (City) -> Unit // Add a listener parameter
) : RecyclerView.Adapter<CityGridAdapter.CityGridViewHolder>() {

    class CityGridViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityGridViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item_city, parent, false) as ImageView
        return CityGridViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: CityGridViewHolder, position: Int) {
        val city = cities[position]
        holder.imageView.load(city.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_foreground)
        }

        // Set the click listener on the item view
        holder.itemView.setOnClickListener {
            onItemClicked(city)
        }
    }

    override fun getItemCount() = cities.size

    fun updateData(newCities: List<City>) {
        this.cities = newCities
        notifyDataSetChanged()
    }
}
