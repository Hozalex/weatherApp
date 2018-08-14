package com.example.ahozyainov.models

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.*

class JSONData(val context: Context)
{
    var cityName: String = ""
    var humidity: String = ""
    var pressure: String = ""
    var wind: String = ""
    var weather: String = ""
    var weatherDescription: String = ""
    private var TAG = "jsonData"

    fun getDataFromJSON(json: JSONObject?)
    {
        try
        {
            weatherDescription = json!!.getJSONArray("weather").getJSONObject(0).getString("main")
            cityName = json.getString("name").toUpperCase(Locale.US) + ", " +
                    json.getJSONObject("sys").getString("country")
            weather = json.getJSONObject("main").getString("temp") + "\u2103" + " " +
                    json.getJSONArray("weather").getJSONObject(0).getString("description")
            humidity = "Humidity: " + json.getJSONObject("main").getString("humidity") + " " + "\u0025"
            pressure = "Pressure: " + json.getJSONObject("main").getString("pressure") + " " + "hpa"
            wind = "Wind: " + json.getJSONObject("wind").getString("speed") + " " + "m/s"
            Log.d(TAG, "from JSONDATA $cityName")
        } catch (e: Exception)
        {
            e.printStackTrace()
            Log.d(TAG, e.message)
        }
        writeDataToDatabase(weatherDescription, humidity, pressure, wind, cityName)
    }

    private fun writeDataToDatabase(weatherDescription: String?, humidity: String, pressure: String, wind: String, cityName: String)
    {
        val databaseHelper = WeatherDatabaseHelper(context)
        val cursor = databaseHelper.getCityWeather()
        cursor.moveToFirst()
        val weatherData = "$weatherDescription, $humidity, $pressure, $wind"

        databaseHelper.cityWeather(cityName, weatherData)

        cursor.close()
        databaseHelper.close()

    }

}