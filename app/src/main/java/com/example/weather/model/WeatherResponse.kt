package com.example.weather.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("fact") val factInfo: FactResponse?
) {
    //десериализуем JSON в наши объекты
    fun toDomain(): List<Weather> {
        return listOf(
            Weather(
                temperature = factInfo?.temperature,
                feelsLike = factInfo?.feels_like,
                condition = factInfo?.condition.orEmpty(),
                icon = factInfo?.icon
            )
        )
    }
}