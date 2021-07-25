package com.example.weather.repository

import com.example.weather.BuildConfig
import com.example.weather.model.WeatherDTO
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

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
        .client(createOkHttpClient(WeatherApiInterceptor()))
        .build().create(WeatherAPI::class.java)

    fun getWeatherDetails(lat: Double, lon: Double, callback: Callback<WeatherDTO>) {
        weatherApi.getWeather(BuildConfig.WEATHER_API_KEY, lat, lon).enqueue(callback)
    }

    //Interceptor — часть библиотеки OkHttp. Посредством Interceptor можно смотреть в логи
    //запросов и ответов,
    private fun createOkHttpClient(interceptor: Interceptor): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(interceptor)
        httpClient.addInterceptor(
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        )
        return httpClient.build()
    }

    inner class WeatherApiInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            return chain.proceed(chain.request())
        }
    }
}