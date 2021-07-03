package com.example.weather.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


//ViewModel сохраняет текущее состояние экрана в процессе пересоздания UI
class MainViewModel(
    private val liveDataToObserve: MutableLiveData<Any> =
        MutableLiveData()
) :
    ViewModel() {

    fun getData(): LiveData<Any> {
        return liveDataToObserve
    }
}
