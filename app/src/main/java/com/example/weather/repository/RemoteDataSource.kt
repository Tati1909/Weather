package com.example.weather.repository

import com.example.weather.BuildConfig
import com.example.weather.model.WeatherDTO
import com.google.gson.GsonBuilder
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Результаты запроса станут обрабатываться во ViewModel — там будет находиться наш callback.

class RemoteDataSource {
    //Запрос создаётся сразу и присваивается переменной weatherApi. Он формируется достаточно
    //просто: через статический builder указываем базовую ссылку, добавляем конвертер —
    // знакомый нам Gson, но работающий теперь «под капотом» Retrofit.
    private val weatherApi = Retrofit.Builder()
        .baseUrl("https://api.weather.yandex.ru/")
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .build().create(WeatherAPI::class.java)

    fun getWeatherDetails(lat: Double, lon: Double, callback: Callback<WeatherDTO>) {
        weatherApi.getWeather(BuildConfig.WEATHER_API_KEY, lat, lon).enqueue(callback)
    }
}