package com.example.weather.view.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weather.R
import com.example.weather.databinding.FragmentMainBinding
import com.example.weather.model.City
import com.example.weather.model.Weather
import com.example.weather.view.ScreenState
import com.example.weather.view.details.DetailsFragment
import com.example.weather.view.showSnackBar
import java.io.IOException

//Будем сохранять состояние приложения между его запусками c пом SharedPreferences
private const val IS_WORLD_KEY = "LIST_OF_TOWNS_KEY"

//У LocationManager мы получаем провайдера GPS. Если это не null, запрашиваем у него
//координаты, передавая следующие аргументы:
private const val REFRESH_PERIOD = 60000L
private const val MINIMAL_DISTANCE = 100f

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    //слушатель, который будет получать новые координаты местоположения пользователя
    private val onLocationListener = object : LocationListener {
        //onLocationChanged вызывается, когда приходят новые данные о местоположении
        override fun onLocationChanged(location: Location) {
            context?.let {
                getAddressAsync(it as FragmentActivity, location)
            }
        }

        //onStatusChanged вызывается при изменении статуса: Available или Unavailable. На
        //Android Q и выше он всегда будет возвращать Available.
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        //onProviderEnabled вызывается, если пользователь включил GPS.
        override fun onProviderEnabled(provider: String) {}

        //onProviderDisabled вызывается, если пользователь выключил GPS или сразу, если GPS
        //был отключён изначально.
        override fun onProviderDisabled(provider: String) {}
    }

    //флаг для загрузки последнего открытого списка городов
    private var isDataSetWorld = false

    private val adapter by lazy {
        CitiesAdapter(::openDetailsFragment)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getLocation()
            } else {
                showRationaleDialog()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        binding.mainFragmentFAB.setOnClickListener { changeCitiesList() }
        binding.mainFragmentFABLocation.setOnClickListener { checkPermission() }
        //подписываемся на LiveData и запрашиваем данные
        viewModel.getScreenState().observe(viewLifecycleOwner) { screenState -> renderData(screenState) }
        viewModel.loadCities(true)
        showListOfTowns()
    }

    private fun checkPermission() {
        activity?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED -> {
                    getLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    showRationaleDialog()
                }
                else -> {
                    requestPermission()
                }
            }
        }
    }

    private fun showRationaleDialog() {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.dialog_rationale_title))
                .setMessage(getString(R.string.dialog_rationale_meaasge))
                .setPositiveButton(getString(R.string.dialog_rationale_give_access)) { _, _ -> requestPermission() }
                .setNegativeButton(getString(R.string.dialog_rationale_decline)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showDialog(title: String, message: String) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.dialog_button_close)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        activity?.let { context ->

            /** Обращаемся к ещё одному системному сервису — LocationManager.
            Это именно тот класс, через который мы будем получать координаты */
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val provider = locationManager.getProvider(LocationManager.GPS_PROVIDER)
                provider?.let {
// Будем получать геоположение через каждые 60 секунд или каждые 100 метров
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        REFRESH_PERIOD,
                        MINIMAL_DISTANCE,
                        onLocationListener
                    )
                }
            } else {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location == null) {
                    showDialog(
                        getString(R.string.dialog_title_gps_turned_off),
                        getString(R.string.dialog_message_last_location_unknown)
                    )
                } else {
                    getAddressAsync(context, location)
                    showDialog(
                        getString(R.string.dialog_title_gps_turned_off),
                        getString(R.string.dialog_message_last_known_location)
                    )
                }
            }
        }
    }

    //С помощью класса Android Geocoder мы можем получить адрес по координатам. Этот класс
    //запрашивает данные у серверов Google по интернету. Поэтому нам нужно не только разрешение на
    //выход в интернет в манифесте, но и отдельный поток для такого запроса. Передаём широту, долготу и
    //желаемое количество адресов по заданным координатам.
    private fun getAddressAsync(
        context: Context,
        location: Location
    ) {
        val geoCoder = Geocoder(context)
        Thread {
            try {
                val addresses = geoCoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                binding.mainFragmentFAB.post {
                    addresses?.get(0)?.let { showAddressDialog(it.getAddressLine(0), location) }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    //Полученный адрес выводим в диалоговом окне. Затем спрашиваем, нужно ли получить погоду по
    //этому адресу. Если погоду нужно, открываем DetailsFragment, куда передаём адрес и координаты.
    //А DetailsFragment обращается на сервер Яндекса как обычно:
    private fun showAddressDialog(address: String, location: Location) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.dialog_address_title))
                .setMessage(address)
                .setPositiveButton(getString(R.string.dialog_address_get_weather)) { _, _ ->
                    openDetailsFragment(
                        Weather(
                            City(
                                address,
                                location.latitude,
                                location.longitude
                            )
                        )
                    )
                }
                .setNegativeButton(getString(R.string.dialog_button_close)) { dialog,
                                                                              _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    //общий метод (для списка и геолокации) для перехода на DetailsFragment
    private fun openDetailsFragment(weather: Weather) {
        //к менеджеру фрагментов обращаемся через activity
        //вместо проверки if (manager != null) -> ставим ? перед apply
        activity?.supportFragmentManager?.apply {
            beginTransaction()
                .add(
                    R.id.container, DetailsFragment.newInstance(Bundle().apply {
                        putParcelable(DetailsFragment.BUNDLE_EXTRA, weather)
                    })
                )
                .addToBackStack("")
                .commitAllowingStateLoss()
        }
    }

    //Получаем у контекста значения настроек SharedPreferences, запрашиваем значение по ключу.
    // Если его там нет, то возвращаем false по умолчанию — так произойдёт при первом запуске.
    // Затем, в зависимости от значения, запрашиваем соответствующие данные у ViewModel.
    private fun showListOfTowns() {
        activity?.let {
            //MODE_PRIVATE - режим доступа к файлу, т е к файлу может обращаться только само приложение
            if (it.getPreferences(Context.MODE_PRIVATE).getBoolean(
                    IS_WORLD_KEY,
                    false
                )
            ) {
                changeCitiesList()
            } else {
                viewModel.loadCities(true)
            }
        }
    }

    private fun changeCitiesList() {
        if (isDataSetWorld) {
            viewModel.loadCities(true)
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_russia)
        } else {
            viewModel.loadCities(false)
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_earth)
        }.also { isDataSetWorld = !isDataSetWorld }

        saveListOfTowns(isDataSetWorld)
    }

    /**
     * Сохраняем настройки SharedPreferences
     * getActivity возвращает FragmentActivity, с которым в настоящее время связан этот фрагмент.
     * Функция apply фиксирует свои изменения в SharedPreferences в памяти немедленно,
     * но запускает асинхронную фиксацию на диск (в порядке очереди),
     * и вы не будете уведомлены о каких-либо сбоях.
     */
    private fun saveListOfTowns(isDataSetWorld: Boolean) {
        activity?.let { fragmentActivity ->
            with(fragmentActivity.getPreferences(Context.MODE_PRIVATE).edit()) {
                //сохраняем настройки: editor.putBoolean("key1", "value1")
                putBoolean(IS_WORLD_KEY, isDataSetWorld)
                apply()
            }
        }
    }

    private fun renderData(appState: ScreenState) {
        when (appState) {
            is ScreenState.Success -> {
                binding.progressBar.visibility = View.GONE
                adapter.setWeather(appState.weatherData)
            }
            is ScreenState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            is ScreenState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.rootView.showSnackBar(
                    getString(R.string.error),
                    getString(R.string.reload),
                    {
                        viewModel.loadCities(true)
                    })
            }
        }
    }

    companion object {

        fun newInstance() =
            MainFragment()
    }
}