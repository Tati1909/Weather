package com.example.weather.viewmodel

import com.example.weather.model.Weather

//класс с состояниями нашего приложения:
// приложение работает
// ошибка
// загрузка данных

sealed class ScreenState {
        data class Success(val weatherData: List<Weather>) : ScreenState()
        data class Error(val error: Throwable) : ScreenState()
        object Loading : ScreenState()
}
