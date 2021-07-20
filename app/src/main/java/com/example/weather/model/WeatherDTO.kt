package com.example.weather.model

data class WeatherDTO(
    val fact: FactDTO?
)

data class FactDTO(
    val temp: Int?,
    val feels_like: Int?,
    //погодные условия (облачно, солнечно)
    val condition: String?
)