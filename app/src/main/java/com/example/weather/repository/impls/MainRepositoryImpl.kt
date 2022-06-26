package com.example.weather.repository.impls

import com.example.weather.model.Weather
import com.example.weather.model.getRussianCities
import com.example.weather.model.getWorldCities
import com.example.weather.repository.api.MainRepository

class MainRepositoryImpl : MainRepository {

    override fun getWeatherFromLocalStorageRus(): List<Weather> = getRussianCities()

    override fun getWeatherFromLocalStorageWorld(): List<Weather> = getWorldCities()
}