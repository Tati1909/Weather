package com.example.weather.repository

import com.example.weather.model.Weather

//разделили получение данных в зависимости от источника
interface MainRepository {
    fun getWeatherFromServer(): Weather
    fun getWeatherFromLocalStorageRus(): List<Weather>
    fun getWeatherFromLocalStorageWorld(): List<Weather>
}