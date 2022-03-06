package com.example.weather.view.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.app.App.Companion.getHistoryDao
import com.example.weather.model.Weather
import com.example.weather.model.WeatherDTO
import com.example.weather.model.convertDtoToModel
import com.example.weather.repository.RemoteDataSource
import com.example.weather.repository.api.DetailsRepository
import com.example.weather.repository.api.LocalRepository
import com.example.weather.repository.impls.DetailsRepositoryImpl
import com.example.weather.repository.impls.LocalRepositoryImpl
import com.example.weather.view.ScreenState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

private const val SERVER_ERROR = "Ошибка сервера"
private const val REQUEST_ERROR = "Ошибка запроса на сервер"
private const val CORRUPTED_DATA = "Неполные данные"

class DetailsViewModel(
    //создаём LiveData для передачи данных
    val detailsLiveData: MutableLiveData<ScreenState> = MutableLiveData(),
    //создаем репозиторий для получения данных
    private val detailsRepository: DetailsRepository = DetailsRepositoryImpl(RemoteDataSource()),
    private val historyRepository: LocalRepository = LocalRepositoryImpl(getHistoryDao())
) : ViewModel() {

    //метод осуществляет запрос на сервер через репозиторий
    fun loadWeather(lat: Double, lon: Double) {
        detailsLiveData.value = ScreenState.Loading
        detailsRepository.getWeatherDetailsFromServer(lat, lon, callBack)
    }

    //сохраняем новый запрос в БД
    fun saveCityToDb(weather: Weather) {
        historyRepository.saveEntity(weather)
    }

    //здесь обрабатывается полученный ответ от сервера и принимается решение о состоянии экрана
    private val callBack = object : Callback<WeatherDTO> {
        @Throws(IOException::class)
        // Вызывается, если ответ от сервера пришёл(даже пустой или с ошибкой)
        override fun onResponse(
            call: Call<WeatherDTO>,
            response: Response<WeatherDTO>
        ) {
            val serverResponse: WeatherDTO? = response.body()
            detailsLiveData.postValue(
                // Синхронизируем поток с потоком UI
                //  isSuccessful - если ответ удачный от 200 до 300 не включая
                if (response.isSuccessful && serverResponse != null) {
                    checkResponse(serverResponse)
                } else {
                    ScreenState.Error(Throwable(SERVER_ERROR))
                }
            )
        }

        // Вызывается при сбое в процессе запроса на сервер
        override fun onFailure(call: Call<WeatherDTO>, t: Throwable) {
            detailsLiveData.postValue(
                ScreenState.Error(
                    Throwable(
                        t.message ?: REQUEST_ERROR
                    )
                )
            )
        }

        //проверяем ответ
        private fun checkResponse(serverResponse: WeatherDTO): ScreenState {
            val fact = serverResponse.factInfo
            //возвращаем или ошибку, или успех
            return if (fact?.temperature == null || fact.feels_like == null || fact.condition.isNullOrEmpty()
            ) {
                ScreenState.Error(Throwable(CORRUPTED_DATA))
            } else {
                ScreenState.Success(convertDtoToModel(serverResponse))
            }
        }
    }
}