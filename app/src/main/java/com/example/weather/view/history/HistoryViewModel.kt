package com.example.weather.view.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.app.App.Companion.getHistoryDao
import com.example.weather.repository.api.LocalRepository
import com.example.weather.repository.impls.LocalRepositoryImpl
import com.example.weather.view.ScreenState

//ViewModel для нового экрана
class HistoryViewModel(
    val historyLiveData: MutableLiveData<ScreenState> = MutableLiveData(),
    private val historyRepository: LocalRepository =
        LocalRepositoryImpl(getHistoryDao())
) : ViewModel() {
    //получение данных из базы
    fun getAllHistory() {
        historyLiveData.value = ScreenState.Loading
        historyLiveData.value = ScreenState.Success(historyRepository.getAllHistory())
    }
}