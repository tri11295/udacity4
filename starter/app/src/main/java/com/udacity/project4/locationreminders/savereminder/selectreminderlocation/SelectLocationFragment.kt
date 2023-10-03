package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemServiceName
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.Locale

class SelectLocationFragment : BaseFragment() {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private var currentMarker: Marker? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
            ) {
                setUpMap()
            } else {
                binding.layoutPermission.visibility = View.VISIBLE
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        binding.mapView.onCreate(savedInstanceState)
        setUpMap()
        setUpEvents()
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    private fun setUpEvents() {
        binding.tvOpenSetting.setOnClickListener {
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }

        binding.btnSave.setOnClickListener {
            if (currentMarker == null) {
                _viewModel.showToast.value = "Please select location"
                return@setOnClickListener
            }
            _viewModel.latitude.value = currentMarker?.position?.latitude
            _viewModel.longitude.value = currentMarker?.position?.longitude
            _viewModel.reminderSelectedLocationStr.postValue(currentMarker?.title)
            findNavController().navigateUp()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpMap() {
        binding.mapView.getMapAsync {
            googleMap = it
            val fineLocationPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val coarseLocationPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val accessBGLocationPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            if (fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                accessBGLocationPermission == PackageManager.PERMISSION_GRANTED
            ) {
                binding.layoutPermission.visibility = View.GONE
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true

                if (currentMarker == null) {
                    locationManager =
                        requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
                    val currentLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    currentLocation?.let { location ->
//                        currentMarker =
//                            googleMap.addMarker(
//                                MarkerOptions().position(
//                                    LatLng(
//                                        location.latitude,
//                                        location.longitude
//                                    )
//                                )
//                                    .title("Current Location")
//                            )
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), 16f
                            )
                        )
                    }
                }
            } else {
                binding.layoutPermission.visibility = View.VISIBLE
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                )
            }
            googleMap.setOnPoiClickListener { poi ->
                updatePointOfInterest(poi)
            }
        }

    }

    private fun updatePointOfInterest(poi: PointOfInterest) {
        currentMarker?.remove()
        currentMarker =
            googleMap.addMarker(
                MarkerOptions().position(poi.latLng)
                    .title(poi.name)
            )
        currentMarker?.showInfoWindow()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, 18f))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        setUpMap()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}