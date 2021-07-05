package com.example.weather.model

//разделили получение данных в зависимости от источника
interface Repository {
    fun getWeatherFromServer(): Weather
    fun getWeatherFromLocalStorage(): Weather

}