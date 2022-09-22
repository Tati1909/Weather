package com.example.weather.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.ItemCityBinding
import com.example.weather.model.Weather

class CitiesAdapter(
    private var onItemClicked: (weather: Weather) -> Unit
) : RecyclerView.Adapter<CitiesAdapter.MainViewHolder>() {

    private var weatherData: List<Weather> = listOf()

    fun setWeather(data: List<Weather>) {
        weatherData = data
        notifyDataSetChanged()
    }

    //создание вьюшки(элемента списка)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_city, parent, false) as
                View
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(weatherData[position])
    }

    override fun getItemCount(): Int {
        return weatherData.size
    }

    inner class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemCityBinding.bind(view)
        fun bind(weather: Weather) {
            binding.name.text = weather.city.city
            binding.item.setOnClickListener { onItemClicked(weather) }
        }
    }
}