package com.example.weather.view.details
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import com.example.weather.R
//import com.example.weather.databinding.FragmentDetailsBinding
//import com.example.weather.model.Weather
//import com.example.weather.viewmodel.AppState
//import com.example.weather.viewmodel.MainViewModel
//import com.google.android.material.snackbar.Snackbar
//
//class DetailsFragment : Fragment() {
//    private var _binding: FragmentDetailsBinding? = null
//
//    private val binding get() = _binding!!
//
//    companion object {
//        fun newInstance() = DetailsFragment()
//    }
//
//    private lateinit var viewModel: MainViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
//        val view = binding.root
//
//        return view
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
//        //подписываемся на обновления при изменении данных в LiveData
//        viewModel.requestLiveData()
//            .observe(viewLifecycleOwner) { renderData(it as AppState) }
//        //запрашиваем данные
//        viewModel.requestWeatherFromLocalSourceRus()
//    }
//
//    private fun renderData(appState: AppState) {
//        when (appState) {
//            is AppState.Success -> {
//                val weatherData = appState.weatherData
//                binding.loadingLayout.visibility = View.GONE
//                setData(weatherData)
//            }
//            is AppState.Loading -> {
//                binding.loadingLayout.visibility = View.VISIBLE
//            }
//            is AppState.Error -> {
//                binding.loadingLayout.visibility = View.GONE
//                Snackbar
//                    .make(binding.mainView, "Error", Snackbar.LENGTH_INDEFINITE)
//                    .setAction("Reload") { viewModel.requestWeatherFromLocalSourceRus() }
//                    .show()
//            }
//        }
//    }
//
//    private fun setData(weatherData: Weather) {
//        binding.cityName.text = weatherData.city.city
//        binding.cityCoordinates.text = String.format(
//            getString(R.string.city_coordinates),
//            weatherData.city.lat.toString(),
//            weatherData.city.lon.toString()
//        )
//        binding.temperatureValue.text = weatherData.temperature.toString()
//        binding.feelsLikeValue.text = weatherData.feelsLike.toString()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        //обязательно обнуляем _binding в destroy, чтобы избежать утечек и нежелаемого поведения
//        _binding = null
//    }
//
//}