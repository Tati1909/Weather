package com.example.weather.repository

import com.example.weather.model.WeatherDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Этим интерфейсом мы описываем конкретный запрос на сервер — запрос на данные погоды с сервера Яндекса.
 * Он формируется простым методом с аннотациями: указывается endpoint ссылки(v2/informers), заголовок (@Header) -
 * в нем мы указываем наш ключ для авторизации на яндекс сервере,
 * а два параметра (@Query) запроса передаются в метод как аргументы.
 * Возвращает метод уже готовый класс WeatherDTO с данными от сервера.
 */

interface ApiService {

    @GET("v2/informers")
    fun getWeather(
        @Header("X-Yandex-API-Key") token: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Call<WeatherDTO>
}