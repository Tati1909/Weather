package com.example.weather.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.AppState
import java.lang.Thread.sleep


//ViewModel сохраняет текущее состояние экрана в процессе пересоздания UI
class MainViewModel(
    private val liveDataToObserve: MutableLiveData<Any> =
        MutableLiveData()
) :
    ViewModel() {

    //получение LiveData
    fun getLiveData() = liveDataToObserve

    //получение данных погоды
    fun getWeather() = getDataFromLocalSource()

    //имитация запроса к БД
    private fun getDataFromLocalSource() {
        liveDataToObserve.value = AppState.Loading
        Thread {
            sleep(1000)
            //сохраняем данные в LiveData
            liveDataToObserve.postValue(AppState.Success(Any()))
        }.start()
    }
}
