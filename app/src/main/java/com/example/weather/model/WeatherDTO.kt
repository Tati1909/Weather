package com.example.weather.model

//Data transfer object - объект передачи данных
data class WeatherDTO(
    val fact: FactDTO?
)

data class FactDTO(
    val temperature: Int?,
    val feelsLike: Int?,
    //погодные условия (облачно, солнечно)
    val condition: String?,
    val icon: String?
)