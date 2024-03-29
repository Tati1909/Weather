package com.example.weather.repository.impls

import com.example.weather.model.WeatherResponse
import com.example.weather.repository.RemoteDataSource
import com.example.weather.repository.api.DetailsRepository
import retrofit2.Callback

class DetailsRepositoryImpl(private val remoteDataSource: RemoteDataSource) : DetailsRepository {

    /** Запрос будет выполнен в отдельном потоке, а результат придет в Callback в main-потоке */
    override fun getWeather(
        lat: Double,
        lon: Double,
        callback: Callback<WeatherResponse>
    ) {
        remoteDataSource.getWeatherDetails(lat, lon, callback)
    }
}