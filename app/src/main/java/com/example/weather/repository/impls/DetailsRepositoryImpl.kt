package com.example.weather.repository.impls

import com.example.weather.model.WeatherDTO
import com.example.weather.repository.DetailsRepository
import com.example.weather.repository.RemoteDataSource

class DetailsRepositoryImpl(private val remoteDataSource: RemoteDataSource) :
    DetailsRepository {
    override fun getWeatherDetailsFromServer(
        lat: Double,
        lon: Double,
        callback: retrofit2.Callback<WeatherDTO>
    ) {
        remoteDataSource.getWeatherDetails(lat, lon, callback)
    }
}