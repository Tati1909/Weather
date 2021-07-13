package com.example.weather.view.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weather.R
import com.example.weather.databinding.FragmentDetailsBinding
import com.example.weather.model.Weather

class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

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
        // if (weather != null) заменяем на let и
        // не заводим доп переменную val weather = getParcelable<Weather>(BUNDLE_EXTRA), но
        // передаем в параметр лямбды weather
        arguments?.getParcelable<Weather>(BUNDLE_EXTRA)?.let { weather ->
            //не заводим доп переменную val city = weather.city, а используем also, но
            // передаем в параметр лямбды city
            weather.city.also { city ->
                binding.cityNameTextView.text = city.city
                binding.cityCoordinatesTextView.text = String.format(
                    getString(R.string.city_coordinates),
                    city.lat.toString(),
                    city.lon.toString()
                )
                binding.temperatureValueTextView.text = weather.temperature.toString()
                binding.feelsLikeValueTextView.text = weather.feelsLike.toString()
            }
        }
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
