package com.example.weather.model

import com.google.gson.annotations.SerializedName

/**
 *     Объект информации о погоде, который включает массив объектов:
 *     temp,feels_like, icon, condition, weend speed и др
 */
data class WeatherResponse(
    @SerializedName("fact")
    val factInfo: FactResponse?
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