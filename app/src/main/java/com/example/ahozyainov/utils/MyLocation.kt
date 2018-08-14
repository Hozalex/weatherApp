package com.example.ahozyainov.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log

class MyLocation
{

    companion object
    {
        private var locationManager: LocationManager? = null
        private lateinit var location: Location
        private const val LOCATION_PROVIDER = LocationManager.PASSIVE_PROVIDER
        private lateinit var geocoder: Geocoder
        private const val TAG = "myLocation"

        @SuppressLint("MissingPermission")
        fun getMyLocation(context: Context): Location
        {
            geocoder = Geocoder(context)
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            location = locationManager!!.getLastKnownLocation(LOCATION_PROVIDER)
            Log.d(TAG, location.latitude.toString() + " " + location.longitude.toString())
            return location
        }


    }


}


