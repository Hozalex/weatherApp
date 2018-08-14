package com.example.ahozyainov.services

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.ahozyainov.activities.R
import com.example.ahozyainov.common.IntentHelper
import com.example.ahozyainov.models.JSONData
import com.example.ahozyainov.widget.WidgetWeather
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherDataLoadService : IntentService("WeatherDataLoadService")
{
    private val POST_URL_API_CITYNAME = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric"
    private val POST_URL_API_COORDINATES = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric"
    private val KEY = "x-api-key"
    private val API_KEY = "b24c3e1ddeea0709848ec2c367c01d24"
    private val RESPONSE_CODE = "cod"
    private val RESPONSE_CODE_OK = 200
    private val TAG: String = "DataLoadServiceLog"
    private lateinit var jsonData: JSONData

    override fun onCreate()
    {
        super.onCreate()
        jsonData = JSONData(context = this)
        Log.d(TAG, "Service Start")
    }

    override fun onHandleIntent(intent: Intent?)
    {
        var jsonObject: JSONObject?
        var url: URL? = null
        if (intent!!.getStringExtra(IntentHelper.EXTRA_CITY_NAME) != "")
        {
            url = URL(String.format(POST_URL_API_CITYNAME, intent.getStringExtra(IntentHelper.EXTRA_CITY_NAME)))
            Log.d(TAG, url.toString())
        }

        try
        {
            val connection: HttpURLConnection = url!!.openConnection() as HttpURLConnection
            connection.addRequestProperty(KEY, API_KEY)

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val rawData = StringBuilder(1024)
            var temp: String

            while (true)
            {
                temp = reader.readLine() ?: break
                rawData.append(temp).append("\n")
            }

            reader.close()
            connection.disconnect()
            jsonObject = JSONObject(rawData.toString())

            if (jsonObject.getInt(RESPONSE_CODE) != RESPONSE_CODE_OK)
            {
                Log.d(TAG, "Response code not OK")
                jsonObject = null
            }

            jsonData.getDataFromJSON(jsonObject)

        } catch (e: Exception)
        {
            e.printStackTrace()
            Log.d(TAG, e.toString())
        }

        sendDataToWeatherActivity()
        sendDataToWidget()
    }

    private fun sendDataToWeatherActivity()
    {
        val responseIntent = Intent(IntentHelper.BROADCAST_ACTION)
        if (jsonData.cityName == "")
        {
            responseIntent.putExtra("cityName", "City not Found")
        } else
        {
            responseIntent.putExtra("cityName", jsonData.cityName)
        }
        responseIntent.putExtra("weather", jsonData.weather)
        responseIntent.putExtra("humidity", jsonData.humidity)
        responseIntent.putExtra("pressure", jsonData.pressure)
        responseIntent.putExtra("wind", jsonData.wind)
        responseIntent.putExtra("weatherDescription", jsonData.weatherDescription)
        Log.d(TAG, "sendDataToWeatherActivity")
        sendBroadcast(responseIntent)
        stopSelf()
    }

    private fun sendDataToWidget()
    {
        val remoteView = RemoteViews(packageName, R.layout.widget_layout)
        if (jsonData.cityName == "")
        {
            remoteView.setTextViewText(R.id.tvCityWidget, "City not found\n$jsonData.weather")
        } else
        {
            remoteView.setTextViewText(R.id.tvCityWidget, jsonData.cityName + "\n" + jsonData.weather)
        }

        when (jsonData.weatherDescription)
        {
            "Clear" -> remoteView.setImageViewResource(R.id.ivCityWidget, R.drawable.sunnywidget)
            "Clouds" -> remoteView.setImageViewResource(R.id.ivCityWidget, R.drawable.cloudlywidget)
            "Rain" -> remoteView.setImageViewResource(R.id.ivCityWidget, R.drawable.rainywidget)
            else -> remoteView.setImageViewResource(R.id.ivCityWidget, R.drawable.start)
        }
        val widget = ComponentName(this, WidgetWeather::class.java)
        val manager = AppWidgetManager.getInstance(this)
        manager.updateAppWidget(widget, remoteView)
        stopSelf()
    }

}