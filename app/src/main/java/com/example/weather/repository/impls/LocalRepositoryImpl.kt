package com.example.weather.repository.impls

import com.example.weather.model.City
import com.example.weather.model.Weather
import com.example.weather.repository.api.LocalRepository
import com.example.weather.room.HistoryDao
import com.example.weather.room.HistoryEntity

class LocalRepositoryImpl(private val localDataSource: HistoryDao) : LocalRepository {
    //получение истории запросов
    override fun getAllHistory(): List<Weather> {
        return convertHistoryEntityToWeather(localDataSource.getAll())
    }

    //сохранение очередного запроса в БД
    override fun saveEntity(weather: Weather) {
        Thread { localDataSource.insert(convertWeatherToEntity(weather)) }.start()
    }

    private fun convertHistoryEntityToWeather(entityList: List<HistoryEntity>): List<Weather> {
        return entityList.map {
            Weather(City(it.city, 0.0, 0.0), it.temperature, 0, it.condition)
        }
    }

    private fun convertWeatherToEntity(weather: Weather): HistoryEntity {
        return HistoryEntity(0, weather.city.city, weather.temperature, weather.condition)
    }
}