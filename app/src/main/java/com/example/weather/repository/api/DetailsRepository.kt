package com.example.weather.repository.api

import com.example.weather.model.WeatherDTO

//Этот интерфейс будет обозначать работу с данными на экране DetailsFragment.
interface DetailsRepository {
    //метод принимает широту, долготу и строку для запроса на сервер и callback для retrofit2
    fun getWeatherDetailsFromServer(
        lat: Double,
        lon: Double,
        callback: retrofit2.Callback<WeatherDTO>
    )
}