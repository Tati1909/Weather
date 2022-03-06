package com.example.weather.model

import com.google.gson.annotations.SerializedName

//Data transfer object - объект передачи данных
data class WeatherDTO(
    /**
     *     Объект информации о погоде, который включает массив объектов:
     *     temp,feels_like, icon, condition, weend speed и др
     */
    @SerializedName("fact")
    val factInfo: FactDTO?
)

data class FactDTO(
    @SerializedName("temp")
    val temperature: Int?,
    @SerializedName("feels_like")
    val feels_like: Int?,
    //погодные условия (облачно, солнечно)
    @SerializedName("condition")
    val condition: String?,
    @SerializedName("icon")
    val icon: String?
)