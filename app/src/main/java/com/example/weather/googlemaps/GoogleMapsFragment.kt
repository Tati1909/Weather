package com.example.weather.googlemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.weather.R
import com.example.weather.databinding.FragmentGoogleMapsSearchBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.io.IOException

class GoogleMapsFragment : Fragment() {

    private var _binding: FragmentGoogleMapsSearchBinding? = null
    private val binding get() = _binding!!
    private val markers: ArrayList<Marker> = arrayListOf()
    private lateinit var map: GoogleMap

    //Здесь мы видим callback, который вызовется, когда карта будет готова к отображению и ей можно
    //будет управлять: добавлять маркеры, линии, слушатели нажатий или двигать камеру к нужным
    //точкам
    private val callback = OnMapReadyCallback { googleMap ->
        //в мар мы будем хранить нашу карту
        map = googleMap
        val initialPlace = LatLng(55.7538337, 37.6211812)
        googleMap.addMarker(
            MarkerOptions().position(initialPlace).title(getString(R.string.marker_start))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(initialPlace))
        //долгое нажатие на карту
        googleMap.setOnMapLongClickListener { latLng ->
            //С помощью класса Geocoder мы будем искать адрес по координатам
            getAddressAsync(latLng)
            //addMarkerToArray берёт текст из EditText, создаёт и добавляет маркер в массив
            addMarkerToArray(latLng)
            //drawLine рисует линию между двумя маркерами
            drawLine()
//Добавим к нашей карте ещё одну полезную функцию — ориентирование по GPS. Это всего один
//небольшой метод, который мы вызовем в нашем callback, когда карта будет готова:
            activateMyLocation(googleMap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_google_maps_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        //initSearchByAddress ищет адрес с помощью Geocoder и центрирует карту на найденном месте
        //initSearchByAddress()
    }

    //С помощью класса Geocoder мы будем искать адрес по координатам
    private fun getAddressAsync(location: LatLng) {
        context?.let {
            val geoCoder = Geocoder(it)
            Thread {
                try {
                    val addresses = geoCoder.getFromLocation(
                        location.latitude,
                        location.longitude, 1
                    )
                    binding.textAddressTextView.post {
                        binding.textAddressTextView.text =
                            addresses?.get(0)?.getAddressLine(0) ?: ""
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    //addMarkerToArray берёт текст из EditText, создаёт и добавляет маркер в массив
    private fun addMarkerToArray(location: LatLng) {
        val marker = setMarker(location, markers.size.toString(), R.drawable.ic_map_pin)
        markers.add(marker)
    }

    private fun setMarker(
        location: LatLng,
        searchText: String,
        resourceId: Int
    ): Marker {
        return map.addMarker(
            MarkerOptions()
                .position(location)
                .title(searchText)
                .icon(BitmapDescriptorFactory.fromResource(resourceId))
        )!!
    }

    //drawLine рисует линию между двумя маркерами
    private fun drawLine() {
        val last: Int = markers.size - 1
        if (last >= 1) {
            val previous: LatLng = markers[last - 1].position
            val current: LatLng = markers[last].position
            map.addPolyline(
                PolylineOptions()
                    .add(previous, current)
                    .color(Color.RED)
                    .width(5f)
            )
        }
    }

    //initSearchByAddress ищет адрес с помощью Geocoder и центрирует карту на найденном месте
    private fun initSearchByAddress() {
        binding.buttonSearch.setOnClickListener {
            val geoCoder = Geocoder(it.context)
            val searchText = binding.searchAddressEditText.text.toString()
            Thread {
                try {
                    val addresses = geoCoder.getFromLocationName(searchText, 1)
                    if (!addresses.isNullOrEmpty() && addresses.size > 0) {
                        goToAddress(addresses, it, searchText)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun goToAddress(addresses: MutableList<Address>, view: View, searchText: String) {
        val location = LatLng(addresses[0].latitude, addresses[0].longitude)
        view.post {
            setMarker(location, searchText, R.drawable.ic_map_marker)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    //Добавим к нашей карте ещё одну полезную функцию — ориентирование по GPS. Это всего один
    //небольшой метод, который мы вызовем в нашем callback, когда карта будет готова:
    @SuppressLint("MissingPermission")
    private fun activateMyLocation(googleMap: GoogleMap) {
        context?.let {
            val isPermissionGranted =
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                        PackageManager.PERMISSION_GRANTED
            googleMap.isMyLocationEnabled = isPermissionGranted
            googleMap.uiSettings.isMyLocationButtonEnabled = isPermissionGranted
        }
//Получить разрешение, если его нет
    }

    companion object {
        fun newInstance() = GoogleMapsFragment()
    }
}