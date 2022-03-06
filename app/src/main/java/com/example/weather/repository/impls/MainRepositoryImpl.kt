package com.example.weather.repository.impls

import com.example.weather.model.Weather
import com.example.weather.model.getRussianCities
import com.example.weather.model.getWorldCities
import com.example.weather.repository.api.MainRepository

class MainRepositoryImpl : MainRepository {
    //то же самое, что и :  override fun getWeatherFromServer(): Weather { return Weather() }
    override fun getWeatherFromServer() = Weather()

    override fun getWeatherFromLocalStorageRus() = getRussianCities()

    override fun getWeatherFromLocalStorageWorld() = getWorldCities()
}