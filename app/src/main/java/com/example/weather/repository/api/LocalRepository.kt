package com.example.weather.repository.api

import com.example.weather.model.Weather

//два метода: получение истории запросов и сохранение очередного запроса в БД.
interface LocalRepository {
    fun getAllHistory(): List<Weather>
    fun saveEntity(weather: Weather)
}