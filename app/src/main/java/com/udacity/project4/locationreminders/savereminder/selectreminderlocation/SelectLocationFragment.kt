package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
//import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


private const val REQUEST_LOCATION_PERMISSION = 0

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map : GoogleMap
    private lateinit var userLocation : LatLng
    private var currentLatLng : LatLng? = null
    private lateinit var fusedLocationClient : FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        onLocationSelected()

        return binding.root
    }

    private fun onLocationSelected() {
        binding.saveButton.setOnClickListener {
            if (::userLocation.isInitialized){
                _viewModel.latitude.value = userLocation.latitude
                _viewModel.longitude.value = userLocation.longitude
                _viewModel.reminderSelectedLocationStr.value = "Location"
                _viewModel.navigationCommand.value = NavigationCommand.BackTo(R.id.saveReminderFragment)
            } else {
                Toast.makeText(context, "Please choose a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap){
        Log.i("location", "here map")
        map = googleMap
        setMapStyle(map)
        getUserLocation()
        setMapLongClick(map)
        setPoiClick(map)
        //Log.i("location", "$userLocation")
    }

    private fun setMapStyle(map : GoogleMap){
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style))

            if (!success){
                Log.e("location", "Style parsing failed")
            }
        }
        catch (e: Resources.NotFoundException){
            Log.e("location", e.message.toString())
        }
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
        ){
            map.isMyLocationEnabled = true

//            var currentLatLng: LatLng

            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 2000

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations){
                        if (location != null){
                            currentLatLng = LatLng(location.latitude, location.longitude)
                            map.addMarker(
                            MarkerOptions().position(currentLatLng!!))
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        }
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper())

            Log.i("location", "$currentLatLng")

        } else {
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun setPoiClick(map : GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng))
            userLocation = poi.latLng
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                getUserLocation()
            } else {
                Snackbar.make(
                    binding.map,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.settings) {
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean{
        map.mapType = when (item.itemId) {
            R.id.normal_map -> {
                Log.i("location", "here")
                GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.hybrid_map -> {
                GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.satellite_map -> {
                GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.terrain_map -> {
                GoogleMap.MAP_TYPE_TERRAIN
            }
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
        return true
    }

    private fun setMapLongClick (map : GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            userLocation = latLng

            map.addMarker(
                MarkerOptions()
                    .position(latLng))
        }
    }

}
