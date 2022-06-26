package com.example.weather.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HistoryEntity(
    //аннотацией @PrimaryKey мы указали, что в таблице — ключ id
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val city: String,
    val temperature: Int?,
    val condition: String?
)