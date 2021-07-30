package com.example.weather.room

import androidx.room.Database
import androidx.room.RoomDatabase

//Это абстрактный класс, у которого должна быть определена аннотация @Database со всеми
//табличными объектами. В нашем случае это массив всего с одной таблицей. Версию БД указываем в
//version. Обязательно увеличиваем версию при изменении структуры БД. Параметр exportSchema
//указывает, создавать или нет файл в проекте, где будет храниться история версий БД. Это
//совершенно необязательно, но в коммерческих проектах — практика хорошая. В классе также
//требуется определить метод, возвращающий объект доступа к данным.
@Database(entities = arrayOf(HistoryEntity::class), version = 1, exportSchema = false)
abstract class HistoryDataBase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}