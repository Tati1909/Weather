package com.example.weather.model

import com.google.gson.annotations.SerializedName

//Data transfer object - объект передачи данных
data class WeatherDTO(
    //Объект фактической информации о погоде
    @SerializedName("fact")
    val factInfo: FactDTO?
)

data class FactDTO(
    @SerializedName("temp")
    val temperature: Int?,
    val feels_like: Int?,
    //погодные условия (облачно, солнечно)
    val condition: String?,
    val icon: String?
)