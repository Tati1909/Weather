package com.example.weather.repository

import com.example.weather.model.Weather
import com.example.weather.model.getRussianCities
import com.example.weather.model.getWorldCities

class MainRepositoryImpl : MainRepository {
    //то же самое, что и :  override fun getWeatherFromServer(): Weather { return Weather() }
    override fun getWeatherFromServer() = Weather()

    override fun getWeatherFromLocalStorageRus() = getRussianCities()

    override fun getWeatherFromLocalStorageWorld() = getWorldCities()
}