package com.example.weather.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.model.Repository
import com.example.weather.model.RepositoryImpl
import java.lang.Thread.sleep


//ViewModel сохраняет текущее состояние экрана в процессе пересоздания UI
class MainViewModel(
    private val liveDataToObserve: MutableLiveData<Any> = MutableLiveData(),
    private val repositoryImpl: Repository = RepositoryImpl()
) :
    ViewModel() {

    //получение LiveData
    fun liveDataRequest() = liveDataToObserve

    //получение данных погоды из локального источника
    fun requestWeatherFromLocalSource() = getDataFromLocalSource()

    //получение данных погоды из сети
    fun requestWeatherFromRemoteSource() = getDataFromLocalSource()

    //имитация запроса к БД
    private fun getDataFromLocalSource() {
        liveDataToObserve.value = AppState.Loading
        Thread {
            sleep(1000)
            //сохраняем данные в LiveData
            liveDataToObserve.postValue(AppState.Success(repositoryImpl.getWeatherFromLocalStorage()))
        }.start()
    }
}
