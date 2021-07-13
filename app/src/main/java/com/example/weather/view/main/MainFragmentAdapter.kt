package com.example.weather.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.model.Weather

class MainFragmentAdapter(private var onItemViewClickListener: MainFragment.OnItemViewClickListener?) :
    RecyclerView.Adapter<MainFragmentAdapter.MainViewHolder>() {

    private var weatherData: List<Weather> = listOf()

    fun setWeather(data: List<Weather>) {
        weatherData = data
        notifyDataSetChanged()
    }

    //создание вьюшки(элемента списка)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_main_recycler_item, parent, false) as
                    View
        )
    }

    //в элемент списка кладем значения
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        //раньше писали holder.bind(weatherData.get(position))
        holder.bind(weatherData[position])
    }

    //получение количества элементов
    override fun getItemCount(): Int {
        return weatherData.size
    }

    fun removeListener() {
        onItemViewClickListener = null
    }

    inner class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //задаем значения при нажатии на элемент списка
        fun bind(weather: Weather) {
            //apply - применяем findViewById и setOnClickListener к itemView
            //чтобы не было повторов
            itemView.apply {
                findViewById<TextView>(R.id.mainFragmentRecyclerItemTextView).text =
                    weather.city.city
                setOnClickListener { onItemViewClickListener?.onItemViewClick(weather) }
            }
        }
    }
}