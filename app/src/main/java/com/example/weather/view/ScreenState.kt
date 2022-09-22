package com.example.weather.view

import com.example.weather.model.Weather

sealed class ScreenState {
        data class Success(val weatherData: List<Weather>) : ScreenState()
        data class Error(val error: Throwable) : ScreenState()
        object Loading : ScreenState()
}
