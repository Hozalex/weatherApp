package com.example.ahozyainov.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.RemoteViews
import com.example.ahozyainov.activities.R
import com.example.ahozyainov.models.JSONData
import com.example.ahozyainov.utils.MyLocation
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class WidgetWeather : AppWidgetProvider()
{
    private val TAG = "widget"
    private lateinit var json: GetJson
    private lateinit var widgetManager: AppWidgetManager
    private lateinit var remoteView: RemoteViews
    private var id = 0

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray)
    {
        remoteView = RemoteViews(context.packageName, R.layout.widget_layout)
        widgetManager = appWidgetManager
        json = GetJson(context)
        for (id in appWidgetIds)
        {
            this.id = id
            json.execute()
            Log.d(TAG, "update $id")
        }

    }

    private fun setDataToWidget(jsonData: JSONData)
    {
        Log.d(TAG, "send data to Widget")
        Log.d(TAG, jsonData.cityName)
        if (jsonData.cityName == "")
        {
            remoteView.setTextViewText(R.id.tvCityWidget, "City not found")
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
        widgetManager.updateAppWidget(id, remoteView)
        Log.d(TAG, "end send data to widget")
    }

    inner class GetJson(var context: Context) : AsyncTask<Void, Void, JSONData>()
    {
        private val API_KEY = "b24c3e1ddeea0709848ec2c367c01d24"
        private val KEY = "x-api-key"
        private val RESPONSE_CODE = "cod"
        private val RESPONSE_CODE_OK = 200
        var jsonData = JSONData(context)

        private val TAG = "getJsonTAG"
        private val location = MyLocation.getMyLocation(context)

        override fun doInBackground(vararg p0: Void?): JSONData
        {
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d(TAG, "coordinates $latitude $longitude")
            var jsonObject: JSONObject?
            try
            {
                val url = URL(String.format("http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric",
                        latitude, longitude))
                Log.d(TAG, "url string " + url.toString())
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
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
                Log.d(TAG, e.message)
            }
            return jsonData
        }

        override fun onPostExecute(jsonData: JSONData)
        {
            setDataToWidget(jsonData)
            Log.d(TAG, "post exec ${jsonData.cityName}")
        }
    }

}