package com.example.weather.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Weather(
    val city: City = City("Москва", 55.755826, 37.617299900000035),
    val temperature: Int? = null,
    val feelsLike: Int? = null,
    val condition: String? = "",
    val icon: String? = ""
) : Parcelable