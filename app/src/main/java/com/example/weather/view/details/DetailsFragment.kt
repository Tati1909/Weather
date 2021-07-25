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
import com.example.weather.model.Weather
import com.example.weather.viewmodel.AppState
import com.example.weather.viewmodel.DetailsViewModel
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.squareup.picasso.Picasso

private const val MAIN_LINK = "https://api.weather.yandex.ru/v2/informers?"

//Здесь создаётся viewModel и подписка на неё
class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    //WeatherBundle мы получим во время создания фрагмента
    //и воспользуемся координатами для составления запроса на сервер
    private lateinit var weatherBundle: Weather
    private val viewModel: DetailsViewModel by lazy {
        ViewModelProvider(this).get(DetailsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weatherBundle = arguments?.getParcelable(BUNDLE_EXTRA) ?: Weather()
        viewModel.detailsLiveData.observe(viewLifecycleOwner) { renderData(it) }
        //получаем данные из удаленного источника
        viewModel.requestWeatherFromRemoteSource(weatherBundle.city.lat, weatherBundle.city.lon)
    }

    //обрабатываем состояние приложения и обеспечиваем корректное отображение на экране
    private fun renderData(appState: AppState) {
        //binding.viewDetailsFragment.visibility = View.VISIBLE
        //binding.loadingLayout.visibility = View.GONE
        when (appState) {
            is AppState.Success -> {
                binding.viewDetailsFragment.visibility = View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
                setWeather(appState.weatherData[0])
            }
            is AppState.Loading -> {
                binding.viewDetailsFragment.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
            }
            is AppState.Error -> {
                binding.viewDetailsFragment.visibility = View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
                /*binding.viewDetailsFragment.showSnackBar(
                    getString(R.string.error),
                    getString(R.string.reload),
                    {
                        viewModel.requestWeatherFromRemoteSource(
                            weatherBundle.city.lat,
                            weatherBundle.city.lon
                        )
                    }) */
            }
        }
    }

    //отображвем данные
    private fun setWeather(weather: Weather) {
        val city = weatherBundle.city
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
