package com.example.weather.viewmodel

import com.example.weather.model.Weather

//класс с состояниями нашего приложения:
// приложение работает
// ошибка
// загрузка данных

sealed class AppState {
        data class Success(val weatherData: Weather) : AppState()
        data class Error(val error: Throwable) : AppState()
        object Loading : AppState()
}
