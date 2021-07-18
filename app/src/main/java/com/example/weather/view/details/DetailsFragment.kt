package com.example.weather.view.details

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.weather.R
import com.example.weather.databinding.FragmentDetailsBinding
import com.example.weather.model.Weather
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

class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    //WeatherBundle мы получим во время создания фрагмента
    //и воспользуемся координатами для составления запроса на сервер
    private lateinit var weatherBundle: Weather

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weatherBundle = arguments?.getParcelable(BUNDLE_EXTRA) ?: Weather()
        binding.mainView.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
        loadWeather()
    }

    companion object {
        //наш ключ-константа,по которому будем находить бандл
        const val BUNDLE_EXTRA = "weather"
        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    //displayWeather отобразит полученные данные
    private fun displayWeather(weatherDTO: WeatherDTO) {
        with(binding) {
            mainView.visibility = View.VISIBLE
            loadingLayout.visibility = View.GONE
            val city = weatherBundle.city
            cityNameTextView.text = city.city
            cityCoordinatesTextView.text = String.format(
                getString(R.string.city_coordinates),
                city.lat.toString(),
                city.lon.toString()
            )
            weatherConditionTextView.text = weatherDTO.fact?.condition
            temperatureValueTextView.text = weatherDTO.fact?.temp.toString()
            feelsLikeValueTextView.text = weatherDTO.fact?.feels_like.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadWeather() {
        try {
            val uri = URL(
                "https://api.weather.yandex.ru/v2/informers?lat=${weatherBundle.city.lat}&lo" +
                        " n=${weatherBundle.city.lon}"
            )
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
                        Gson().fromJson(
                            getLines(bufferedReader),
                            WeatherDTO::class.java
                        )
                    //вызываем метод в рабочем потоке
                    handler.post { displayWeather(weatherDTO) }
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
            //Обработка ошибки
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
}
