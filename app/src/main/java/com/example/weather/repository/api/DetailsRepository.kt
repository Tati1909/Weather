package com.example.weather.repository.api

import com.example.weather.model.WeatherResponse

interface DetailsRepository {
    fun getWeather(
        lat: Double,
        lon: Double,
        callback: retrofit2.Callback<WeatherResponse>
    )
}