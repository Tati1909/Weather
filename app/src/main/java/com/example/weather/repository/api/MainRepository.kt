package com.example.weather.repository.api

import com.example.weather.model.Weather

//разделили получение данных в зависимости от источника
interface MainRepository {
    fun getWeatherFromLocalStorageRus(): List<Weather>
    fun getWeatherFromLocalStorageWorld(): List<Weather>
}