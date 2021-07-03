package com.example.weather.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.Thread.sleep


//ViewModel сохраняет текущее состояние экрана в процессе пересоздания UI
class MainViewModel(
    private val liveDataToObserve: MutableLiveData<Any> =
        MutableLiveData()
) :
    ViewModel() {

    fun getData(): LiveData<Any> {
        //передаем значения в LiveData
        getDataFromLocalSource()
        return liveDataToObserve
    }

    //имитация запроса к БД
    private fun getDataFromLocalSource() {
        Thread {
            sleep(1000)
            //сохраняем данные в LiveData
            liveDataToObserve.postValue(Any())
        }.start()
    }
}
