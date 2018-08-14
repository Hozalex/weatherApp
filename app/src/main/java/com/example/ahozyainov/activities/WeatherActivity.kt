package com.example.ahozyainov.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.ahozyainov.common.IntentHelper
import com.example.ahozyainov.common.IntentHelper.BROADCAST_ACTION
import com.example.ahozyainov.models.WeatherDatabaseHelper
import com.example.ahozyainov.services.WeatherDataLoadService
import kotlinx.android.synthetic.main.activity_weather.*

class WeatherActivity : AppCompatActivity()
{

    private lateinit var lastShare: String
    private lateinit var getIntent: Intent
    private var isPressureChecked: Boolean = false
    private var isHumidityChecked: Boolean = false
    private var isWindChecked: Boolean = false
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        getIntent = intent
        lastShare = resources.getString(R.string.last_share_weather)

        broadcastReceiver = WeatherBroadcastReceiver()
        registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_ACTION))



        checkWeatherDetails()
        updateWeatherData(getIntent.getStringExtra(IntentHelper.EXTRA_CITY_NAME))

        share_button.setOnClickListener {
            shareWeather()

        }
    }

    override fun onStop()
    {
        unregisterReceiver(broadcastReceiver)
        super.onStop()
    }

    private fun shareWeather()
    {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_TEXT, text_view_city.text.toString() + ": " +
                text_view_weather.text.toString())
        shareIntent.type = "text/plain"

        if (shareIntent.resolveActivity(packageManager) != null)
        {
            startActivity(shareIntent)
        }

        val sendIntent: Intent = intent
        sendIntent.putExtra(IntentHelper.EXTRA_SHARED_WEATHER, lastShare + " " + text_view_city.text.toString())
        setResult(Activity.RESULT_OK, intent)
    }

    private fun checkWeatherDetails()
    {
        if (intent.getBooleanExtra(IntentHelper.EXTRA_CHECKBOX_PRESSURE, false))
        {
            isPressureChecked = true
        }
        if (intent.getBooleanExtra(IntentHelper.EXTRA_CHECKBOX_HUMIDITY, false))
        {
            isHumidityChecked = true
        }
        if (intent.getBooleanExtra(IntentHelper.EXTRA_CHECKBOX_WIND, false))
        {
            isWindChecked = true
        }
    }

    private fun updateWeatherData(city: String)
    {

        val serviceIntent = Intent(this, WeatherDataLoadService::class.java)
        serviceIntent.putExtra(IntentHelper.EXTRA_CITY_NAME, city)
        serviceIntent.putExtra(IntentHelper.EXTRA_COORDINATES, arrayOf(""))
        startService(serviceIntent)

    }

    @SuppressLint("SetTextI18n")
    fun renderWeather(cityName: String, weather: String, humidity: String, pressure: String, wind: String, weatherDescription: String)
    {

        try
        {
            text_view_city.text = cityName
            text_view_weather.text = weather
            writeDataToDatabase(weatherDescription, humidity, pressure, wind, cityName)

            when (weatherDescription)
            {
                "Clear" -> image_weather_activity.setImageResource(R.drawable.sunny)
                "Clouds" -> image_weather_activity.setImageResource(R.drawable.cloudly)
                "Rain" -> image_weather_activity.setImageResource(R.drawable.rainy)
                else -> image_weather_activity.setImageResource(R.drawable.start)
            }
            if (isPressureChecked)
            {
                text_view_pressure.text = pressure
                text_view_pressure.setBackgroundColor(this.resources.getColor(R.color.colorBackground))
            }
            if (isHumidityChecked)
            {
                text_view_humidity.text = humidity
                text_view_humidity.setBackgroundColor(this.resources.getColor(R.color.colorBackground))
            }
            if (isWindChecked)
            {
                text_view_wind.text = wind
                text_view_wind.setBackgroundColor(this.resources.getColor(R.color.colorBackground))
            }

        } catch (e: Exception)
        {
            e.printStackTrace()
        }

    }

    private fun writeDataToDatabase(weatherDescription: String?, humidity: String, pressure: String, wind: String, cityName: String)
    {
        val databaseHelper = WeatherDatabaseHelper(context = this)
        val cursor = databaseHelper.getCityWeather()
        cursor.moveToFirst()
        val weatherData = "$weatherDescription, $humidity, $pressure, $wind"

        databaseHelper.cityWeather(cityName, weatherData)

        cursor.close()
        databaseHelper.close()

    }

    inner class WeatherBroadcastReceiver : BroadcastReceiver()
    {
        private lateinit var cityName: String
        private lateinit var humidity: String
        private lateinit var pressure: String
        private lateinit var wind: String
        private lateinit var weather: String
        private lateinit var weatherDescription: String


        override fun onReceive(p0: Context, p1: Intent)
        {
            cityName = p1.getStringExtra("cityName")
            weather = p1.getStringExtra("weather")
            humidity = p1.getStringExtra("humidity")
            pressure = p1.getStringExtra("pressure")
            wind = p1.getStringExtra("wind")
            weatherDescription = p1.getStringExtra("weatherDescription")

            renderWeather(cityName, weather, humidity, pressure, wind, weatherDescription)

        }


    }


}



