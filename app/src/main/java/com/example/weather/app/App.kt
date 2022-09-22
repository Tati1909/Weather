package com.example.weather.app

import android.app.Application
import androidx.room.Room
import com.example.weather.room.HistoryDao
import com.example.weather.room.HistoryDataBase

//Наиболее
//распространённая практика — создание базы на уровне приложения. В таком случае база создаётся
//один раз на протяжении жизненного цикла приложения и доступна всем через статический метод. Для
//этого создадим класс App, который будет наследовать Application-класс приложения. Класс
//Application формируется до создания первой активити и имеет доступ к контексту.
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }

    companion object {

        private var appInstance: App? = null
        private var db: HistoryDataBase? = null
        private const val DB_NAME = "History.db"

        fun getHistoryDao(): HistoryDao {
            if (db == null) {
                synchronized(HistoryDataBase::class.java) {
                    if (db == null) {
                        if (appInstance == null) throw
                        IllegalStateException("Application is null while creating DataBase")
                        db = Room
                            //если БД ещё не создана, то мы потокобезопасно формируем базу через метод databaseBuilder
                            .databaseBuilder(
                                appInstance!!.applicationContext,
                                HistoryDataBase::class.java,
                                DB_NAME
                            )
                            //allowMainThreadQueries позволяет делать запросы из основного потока
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return db?.historyDao() ?: throw IllegalStateException("Data Base should not be null")
        }
    }
}