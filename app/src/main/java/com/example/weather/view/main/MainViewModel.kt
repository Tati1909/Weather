package com.example.weather.view.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.repository.api.MainRepository
import com.example.weather.repository.impls.MainRepositoryImpl
import com.example.weather.view.ScreenState

//ViewModel сохраняет текущее состояние экрана в процессе пересоздания UI
class MainViewModel(
    private val liveDataToObserve: MutableLiveData<ScreenState> = MutableLiveData(),
    private val repository: MainRepository = MainRepositoryImpl()
) :
    ViewModel() {

    fun getScreenState(): MutableLiveData<ScreenState> = liveDataToObserve

    fun loadCities(isRussian: Boolean) {
        liveDataToObserve.value = ScreenState.Loading
        Thread {
            liveDataToObserve.postValue(
                ScreenState.Success(
                    if (isRussian) {
                        repository.getWeatherFromLocalStorageRus()
                    } else repository.getWeatherFromLocalStorageWorld()
                )
            )
        }.start()
    }
}
