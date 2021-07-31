package com.example.weather.view.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.weather.R
import com.example.weather.databinding.FragmentMainBinding
import com.example.weather.model.Weather
import com.example.weather.view.details.DetailsFragment
import com.example.weather.viewmodel.MainViewModel
import com.example.weather.viewmodel.ScreenState
import com.google.android.material.snackbar.Snackbar

//Будем сохранять состояние приложения между его запусками c пом SharedPreferences
private const val IS_WORLD_KEY = "LIST_OF_TOWNS_KEY"

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    //Делегирование: private lateinit var viewModel: MainViewModel
    //Теперь наша ViewModel создаётся через ленивую инициализацию, а не в методе onViewCreated
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    //флаг: следит за "подгрузкой" данных
    private var isDataSetRus: Boolean = true

    //флаг для загрузки последнего открытого списка городов
    private var isDataSetWorld: Boolean = false

    //создаем интерфейс(через object) и передаем его в адаптер
    private val adapter = MainFragmentAdapter(object : OnItemViewClickListener {
        override fun onItemViewClick(weather: Weather) {
            //к менеджеру фрагментов обращаемся через activity
            //вместо проверки if (manager != null) -> ставим ? перед apply
            activity?.supportFragmentManager?.apply {
                beginTransaction()
                    .add(R.id.container, DetailsFragment.newInstance(Bundle().apply {
                        putParcelable(DetailsFragment.BUNDLE_EXTRA, weather)
                    }))
                    .addToBackStack("")
                    .commitAllowingStateLoss()
            }
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
        viewModel.requestLiveData().observe(viewLifecycleOwner) { renderData(it as ScreenState) }
        viewModel.requestWeatherFromLocalSourceRus()

        showListOfTowns()
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