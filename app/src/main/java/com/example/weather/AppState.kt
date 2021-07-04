package com.example.weather

//класс с состояниями нашего приложения:
// приложение работает,
// загрузка данных
// ошибка

sealed class AppState {
        data class Success(val weatherData: Any) : AppState()
        data class Error(val error: Throwable) : AppState()
        object Loading : AppState()
}
