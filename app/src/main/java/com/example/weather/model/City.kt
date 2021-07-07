package com.example.weather.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(
    val city: String,
    //широта
    val lat: Double,
    //долгота
    val lon: Double
) : Parcelable
