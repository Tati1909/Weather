package com.example.weather.model

class RepositoryImpl : Repository {
    //то же самое, что и :  override fun getWeatherFromServer(): Weather { return Weather() }
    override fun getWeatherFromServer() = Weather()

    override fun getWeatherFromLocalStorageRus() = getRussianCities()

    override fun getWeatherFromLocalStorageWorld() = getWorldCities()
}