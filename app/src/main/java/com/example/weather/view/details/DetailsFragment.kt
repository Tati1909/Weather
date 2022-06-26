package com.example.weather.view.details

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.weather.R
import com.example.weather.databinding.FragmentDetailsBinding
import com.example.weather.model.City
import com.example.weather.model.Weather
import com.example.weather.view.ScreenState
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.squareup.picasso.Picasso

//Здесь создаётся viewModel и подписка на неё
class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    //WeatherBundle мы получим во время создания фрагмента
    //и воспользуемся координатами для составления запроса на сервер
    private lateinit var weatherBundle: Weather
    private val viewModel: DetailsViewModel by lazy {
        ViewModelProvider(this)[DetailsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weatherBundle = arguments?.getParcelable(BUNDLE_EXTRA) ?: Weather()
        viewModel.detailsLiveData.observe(viewLifecycleOwner) { renderData(it) }
        //получаем данные из удаленного источника
        viewModel.loadWeather(weatherBundle.city.lat, weatherBundle.city.lon)
    }

    //обрабатываем состояние приложения и обеспечиваем корректное отображение на экране
    private fun renderData(appState: ScreenState) {
        when (appState) {
            is ScreenState.Success -> {
                binding.viewDetailsFragment.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                showWeather(appState.weatherData[0])
            }
            is ScreenState.Loading -> {
                binding.viewDetailsFragment.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
            is ScreenState.Error -> {
                binding.viewDetailsFragment.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.viewDetailsFragment.showSnackBarDetail(
                    getString(R.string.error),
                    getString(R.string.reload),
                    {
                        viewModel.loadWeather(
                            weatherBundle.city.lat,
                            weatherBundle.city.lon
                        )
                    })
            }
        }
    }

    private fun showWeather(weather: Weather) {
        val city = weatherBundle.city
        //сохраняем новый запрос в БД
        saveCityToDataBase(city, weather)
        binding.cityNameTextView.text = city.city
        binding.cityCoordinatesTextView.text = String.format(
            getString(R.string.city_coordinates),
            city.lat.toString(),
            city.lon.toString()
        )
        binding.temperatureValueTextView.text = weather.temperature.toString()
        binding.feelsLikeValueTextView.text = weather.feelsLike.toString()
        binding.weatherConditionTextView.text = weather.condition
        //загружаем картинку города
        Picasso
            .get()
            .load("https://freepngimg.com/thumb/city/36275-3-city-hd.png")
            .into(binding.headerIcon)

        // загружаем тучку и солнышко
        weather.icon?.let {
            GlideToVectorYou.justLoadImage(
                activity,
                Uri.parse("https://yastatic.net/weather/i/icons/blueye/color/svg/${it}.svg"),
                binding.weatherIcon
            )
        }
    }

    //сохраняем новый запрос в БД
    private fun saveCityToDataBase(city: City, weather: Weather) {
        viewModel.saveCityToDb(
            Weather(
                city,
                weather.temperature,
                weather.feelsLike,
                weather.condition
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}
