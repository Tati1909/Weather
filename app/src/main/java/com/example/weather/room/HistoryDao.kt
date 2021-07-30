package com.example.weather.room

import androidx.room.*

//Аннотации @Insert, @Update, @Delete управляют модификацией данных. А аннотация @Query
//создаёт запросы к базе данных.
//Если поле, которое мы хотим добавить, уже есть, то выбираем соответствующую стратегию
//OnConflictStrategy (IGNORE, ABORT, REPLACE).
@Dao
interface HistoryDao {
    @Query("SELECT * FROM HistoryEntity")
    fun all(): List<HistoryEntity>

    @Query("SELECT * FROM HistoryEntity WHERE city LIKE :city")
    fun getDataByWord(city: String): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: HistoryEntity)

    @Update
    fun update(entity: HistoryEntity)

    @Delete
    fun delete(entity: HistoryEntity)
}