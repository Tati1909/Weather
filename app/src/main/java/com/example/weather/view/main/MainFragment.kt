package com.example.weather.view.main

import android.Manifest
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weather.R
import com.example.weather.databinding.FragmentMainBinding
import com.example.weather.model.City
import com.example.weather.model.Weather
import com.example.weather.view.details.DetailsFragment
import com.example.weather.viewmodel.MainViewModel
import com.example.weather.viewmodel.ScreenState
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

//Будем сохранять состояние приложения между его запусками c пом SharedPreferences
private const val IS_WORLD_KEY = "LIST_OF_TOWNS_KEY"
const val REQUEST_CODE = 42

//У LocationManager мы получаем провайдера GPS. Если это не null, запрашиваем у него
//координаты, передавая следующие аргументы:
private const val REFRESH_PERIOD = 60000L
private const val MINIMAL_DISTANCE = 100f

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    //Делегирование: private lateinit var viewModel: MainViewModel
    //Теперь наша ViewModel создаётся через ленивую инициализацию, а не в методе onViewCreated
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
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

    //создаем интерфейс(через object) и передаем его в адаптер
    private val adapter = MainFragmentAdapter(object : OnItemViewClickListener {
        override fun onItemViewClick(weather: Weather) {
            openDetailsFragment(weather)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainFragmentRecyclerView.adapter = adapter
        binding.mainFragmentFAB.setOnClickListener { changeWeatherDataSet() }
        //checkPermission проверяет, имеет ли данный пакет данное разрешение и
        // разрешена ли операция приложения, соответствующая этому разрешению.
        binding.mainFragmentFABLocation.setOnClickListener { checkPermission() }
        viewModel.requestLiveData().observe(viewLifecycleOwner) { renderData(it as ScreenState) }
        viewModel.requestWeatherFromLocalSourceRus()

        showListOfTowns()
    }

    //checkPermission проверяет, имеет ли данный пакет в UID и PID данное разрешение и
    // разрешена ли операция приложения, соответствующая этому разрешению.
    private fun checkPermission() {
        activity?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    getLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                -> {
                    showRationaleDialog()
                } //повторный запрос
                else -> {
                    requestPermission()
                }
            }
        }
    }

    //Если пользователь уже отказывал в разрешении, то отображаем диалоговое окно с объяснением,
    //прежде чем запрашивать доступ:
    private fun showRationaleDialog() {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.dialog_rationale_title))
                .setMessage(getString(R.string.dialog_rationale_meaasge))
                .setPositiveButton(getString(R.string.dialog_rationale_give_access))
                { _, _ ->
                    requestPermission()
                }
                .setNegativeButton(getString(R.string.dialog_rationale_decline)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    //Если доступа на поиск GPS нет, то запрашиваем разрешение
    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        checkPermissionsResult(requestCode, grantResults)
    }

    private fun checkPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                var grantedPermissions = 0
                if ((grantResults.isNotEmpty())) {
                    for (i in grantResults) {
                        if (i == PackageManager.PERMISSION_GRANTED) {
                            grantedPermissions++
                        }
                    }
                    if (grantResults.size == grantedPermissions) {
                        getLocation()
                    } else {
                        showDialog(
                            //Если разрешения нет, то отображаем диалоговое окно. В нём уведомляем
                            // пользователя, что для получения прогноза погоды по координатам,
                            // нужно дать разрешение на доступ к GPS
                            getString(R.string.dialog_title_no_gps),
                            getString(R.string.dialog_message_no_gps)
                        )
                    }
                } else {
                    showDialog(
                        //Если разрешения нет, то отображаем диалоговое окно. В нём уведомляем
                        // пользователя, что для получения прогноза погоды по координатам,
                        // нужно дать разрешение на доступ к GPS
                        getString(R.string.dialog_title_no_gps),
                        getString(R.string.dialog_message_no_gps)
                    )
                }
                return
            }
        }
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

    //Если пользователь дал разрешение, получаем местоположение
    private fun getLocation() {
        activity?.let { context ->
            //Если всё в порядке, обращаемся к ещё одному системному сервису — LocationManager.
            // Это именно тот класс, через который мы будем получать координаты
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
// Получить менеджер геолокаций
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as
                            LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val provider =
                        locationManager.getProvider(LocationManager.GPS_PROVIDER)
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
                    val location =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
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
            } else {
                //На всякий случай проверяем разрешения. Если их нет, вызываем showRationaleDialog
                showRationaleDialog()
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
                    showAddressDialog(addresses[0].getAddressLine(0), location)
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
    private fun openDetailsFragment(
        weather: Weather
    ) {
        //к менеджеру фрагментов обращаемся через activity
        //вместо проверки if (manager != null) -> ставим ? перед apply
        activity?.supportFragmentManager?.apply {
            beginTransaction()
                .add(
                    R.id.container,
                    DetailsFragment.newInstance(Bundle().apply {
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
                changeWeatherDataSet()
            } else {
                viewModel.requestWeatherFromLocalSourceRus()
            }
        }
    }

    override fun onDestroy() {
        adapter.removeListener()
        super.onDestroy()
    }

    private fun changeWeatherDataSet() {
        if (isDataSetWorld) {
            viewModel.requestWeatherFromLocalSourceRus()
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_russia)
        } else {
            viewModel.requestWeatherFromLocalSourceWorld()
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_earth)
        }.also { isDataSetWorld = !isDataSetWorld }

        saveListOfTowns(isDataSetWorld)
    }

    //записываем настройки SharedPreferences
    private fun saveListOfTowns(isDataSetWorld: Boolean) {
        activity?.let {
            with(it.getPreferences(Context.MODE_PRIVATE).edit()) {
                //сохраняем настройки: editor.putBoolean("key1", "value1")
                putBoolean(IS_WORLD_KEY, isDataSetWorld)
                apply()
            }
        }
    }

    private fun renderData(appState: ScreenState) {
        when (appState) {
            is ScreenState.Success -> {
                binding.mainFragmentLoadingLayout.visibility = View.GONE
                adapter.setWeather(appState.weatherData)
            }
            is ScreenState.Loading -> {
                binding.mainFragmentLoadingLayout.visibility = View.VISIBLE
            }
            is ScreenState.Error -> {
                binding.mainFragmentLoadingLayout.visibility = View.GONE
                binding.mainFragmentRootView.showSnackBar(
                    getString(R.string.error),
                    getString(R.string.reload),
                    {
                        viewModel.requestWeatherFromLocalSourceRus()
                    })
            }
        }
    }

    // Создадим extension-функцию для Snackbar (при ошибке приложения)
    // будем ее использовать в renderData у корневого экрана fragment_main.xml
    private fun View.showSnackBar(
        text: String,
        actionText: String,
        action: (View) -> Unit,
        length: Int = Snackbar.LENGTH_INDEFINITE
    ) {
        Snackbar.make(this, text, length).setAction(actionText, action).show()
    }

    //этот интерфейс использует холдер
    interface OnItemViewClickListener {
        fun onItemViewClick(weather: Weather)
    }

    companion object {
        fun newInstance() =
            MainFragment()
    }
}