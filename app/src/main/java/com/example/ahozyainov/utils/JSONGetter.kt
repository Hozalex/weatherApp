package com.example.ahozyainov.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.example.ahozyainov.models.JSONData
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class JSONGetter(context: Context) : AsyncTask<Void, Void, JSONData>()
{
    private val API_KEY = "b24c3e1ddeea0709848ec2c367c01d24"
    private val KEY = "x-api-key"
    private val RESPONSE_CODE = "cod"
    private val RESPONSE_CODE_OK = 200
    var jsonData = JSONData(context)
    var name = ""
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
        name = jsonData.cityName
        return jsonData
    }

    override fun onPostExecute(jsonData: JSONData)
    {
        Log.d(TAG, "post exec ${jsonData.cityName}")
    }
}