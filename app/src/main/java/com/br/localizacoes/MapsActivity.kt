package com.br.localizacoes

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.contentValuesOf

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.br.localizacoes.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    companion object{
        const val LOCATION_PERMISSION_REQUEST_CODE: Int=9090
        const val REQUEST_CHECK_SETTINGS = 2
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lasLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private var locationUpdateState = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lasLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lasLocation.latitude, lasLocation.longitude))
            }
        }

        createLocationRequest()
    }
    private fun placeMarkerOnMap(location: LatLng){
        val markerOptions = MarkerOptions().position(location)
       /* markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.ic_luacher_user_location)))
*/
        map.addMarker(markerOptions)
    }


    private fun createLocationRequest(){
        locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val  builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener {e->
            if (e is ResolvableApiException){
                try {
                    e.startResolutionForResult(this@MapsActivity, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.getUiSettings().isZoomControlsEnabled
        map.setOnMarkerClickListener(this)
        setUpMap()
    }

    private fun startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this,
            ACCESS_FINE_LOCATION
            )!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
        val inteiro: Int = 90
        val long: Long = inteiro.toLong()

    }

    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION
            )!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) {location->
        if (location != null){
            val currentLocation = LatLng(location.latitude, location.longitude)
            placeMarkerOnMap(currentLocation)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 42f))
            val endereco = lasLocation
            Toast.makeText(this, "Endereco: "+endereco, Toast.LENGTH_SHORT).show()
        }

        }
    }

    override fun onMarkerClick(p0: Marker)=false
}

