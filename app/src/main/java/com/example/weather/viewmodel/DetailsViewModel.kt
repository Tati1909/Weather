package com.example.weather.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.model.WeatherDTO
import com.example.weather.model.convertDtoToModel
import com.example.weather.repository.DetailsRepository
import com.example.weather.repository.DetailsRepositoryImpl
import com.example.weather.repository.RemoteDataSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

private const val SERVER_ERROR = "Ошибка сервера"
private const val REQUEST_ERROR = "Ошибка запроса на сервер"
private const val CORRUPTED_DATA = "Неполные данные"

class DetailsViewModel(
    //создаём LiveData для передачи данных
    val detailsLiveData: MutableLiveData<AppState> = MutableLiveData(),
    //создаем репозиторий для получения данных
    private val detailsRepositoryImpl: DetailsRepository =
        DetailsRepositoryImpl(RemoteDataSource())
) : ViewModel() {

    /*//метод возвращает LiveData, чтобы на неё подписаться
    fun getLiveData() = detailsLiveData  */

    //метод осуществляет запрос на сервер через репозиторий
    fun requestWeatherFromRemoteSource(lat: Double, lon: Double) {
        detailsLiveData.value = AppState.Loading
        detailsRepositoryImpl.getWeatherDetailsFromServer(lat, lon, callBack)
    }

    //здесь обрабатывается полученный ответ от сервера и принимается решение о состоянии экрана
    private val callBack = object : Callback<WeatherDTO> {
        @Throws(IOException::class)
        // Вызывается, если ответ от сервера пришёл
        override fun onResponse(
            call: Call<WeatherDTO>, response:
            Response<WeatherDTO>
        ) {
            val serverResponse: WeatherDTO? = response.body()
            detailsLiveData.postValue(
                // Синхронизируем поток с потоком UI
                if (response.isSuccessful && serverResponse != null) {
                    checkResponse(serverResponse)
                } else {
                    AppState.Error(Throwable(SERVER_ERROR))
                }
            )
        }

        // Вызывается при сбое в процессе запроса на сервер
        override fun onFailure(call: Call<WeatherDTO>, t: Throwable) {
            detailsLiveData.postValue(
                AppState.Error(
                    Throwable(
                        t.message ?: REQUEST_ERROR
                    )
                )
            )
        }

        //проверяем ответ
        private fun checkResponse(serverResponse: WeatherDTO): AppState {
            val fact = serverResponse.fact
            return if (fact == null || fact.temperature == null || fact.feelsLike ==
                null || fact.condition.isNullOrEmpty()
            ) {
                AppState.Error(Throwable(CORRUPTED_DATA))
            } else {
                AppState.Success(convertDtoToModel(serverResponse))
            }
        }
    }
}