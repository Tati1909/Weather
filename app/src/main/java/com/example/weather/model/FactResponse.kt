package com.example.weather.model

import com.google.gson.annotations.SerializedName

data class FactResponse(
    @SerializedName("temp") val temperature: Int?,
    @SerializedName("feels_like") val feels_like: Int?,
    //погодные условия (облачно, солнечно)
    @SerializedName("condition") val condition: String?,
    @SerializedName("icon") val icon: String?
)