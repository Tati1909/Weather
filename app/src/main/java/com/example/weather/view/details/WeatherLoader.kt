package com.example.weather.view.details

import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.weather.model.WeatherDTO
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection

//наш ключ разработчика
private const val YOUR_API_KEY = "5de466c9-593a-4af8-b867-f8e318a353ef"

//Класс, отвечающий за загрузку данных из интернета, и
//callback-уведомление о событиях загрузки
@RequiresApi(Build.VERSION_CODES.N)
class WeatherLoader(
    private val listener: WeatherLoaderListener,
    private val lat: Double,
    private val lon: Double
) {
    fun loadWeather() {
        try {
            val uri = URL("https://api.weather.yandex.ru/v2/informers?lat=${lat}&lon=${lon}")
            //Handler создаётся в UI-потоке, а ВЫЗЫВАЕТ метод post в рабочем потоке
            val handler = Handler()
            Thread(Runnable {
                lateinit var urlConnection: HttpsURLConnection
                try {
                    //формирование запроса urlConnection
                    urlConnection = uri.openConnection() as HttpsURLConnection
                    urlConnection.requestMethod = "GET"
                    //добавление заголовка в запрос urlConnection
                    urlConnection.addRequestProperty(
                        "X-Yandex-API-Key",
                        YOUR_API_KEY
                    )
                    urlConnection.readTimeout = 10000
                    val bufferedReader =
                        BufferedReader(InputStreamReader(urlConnection.inputStream))
                    // преобразование ответа от сервера (JSON) в модель данных (WeatherDTO)
                    val weatherDTO: WeatherDTO =
                        Gson().fromJson(getLines(bufferedReader), WeatherDTO::class.java)
                    //вызываем метод в рабочем потоке
                    handler.post { listener.onLoaded(weatherDTO) }
                } catch (e: Exception) {
                    Log.e("", "Fail connection", e)
                    e.printStackTrace()
                    //Обработка ошибки
                } finally {
                    urlConnection.disconnect()
                }
            }).start()
        } catch (e: MalformedURLException) {
            Log.e("", "Fail URI", e)
            e.printStackTrace()
            //TODO Обработка ошибки
            listener.onFailed(e)
        }
    }

    //читаем данные
    //используется аннотация, т к lines() у ридера добавился в 30 API
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getLines(reader: BufferedReader): String {
        //lines - Возвращает Stream, элементами которого являются строки,
        // прочитанные из этого BufferedReader. Поле Stream заполняется лениво, т. Е.
        // выполняется только чтение во время операции терминального потока .
        return reader.lines().collect(Collectors.joining("\n"))
    }

    //Интерфейс мы будем использовать в качестве метода обратного вызова с результатами загрузки:
    interface WeatherLoaderListener {
        //загружен
        fun onLoaded(weatherDTO: WeatherDTO)

        //неудачно
        fun onFailed(throwable: Throwable)
    }
}