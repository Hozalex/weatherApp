package com.example.ahozyainov.models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build.ID
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal.CITY
import android.util.Log
import com.example.ahozyainov.adapters.CityAdapter
import java.text.DateFormat
import java.util.*

class WeatherDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)
{
    val TAG = javaClass.simpleName
    val TABLE = DB_NAME

    val CITY = "CITY"
    val ID = "_id"
    val WEATHER_DATA = "WEATHER_DATA"
    val DATETIME = "DATETIME"
    val DATABASE_CREATE = "CREATE TABLE " + TABLE + " (" +
            "$ID integer PRIMARY KEY autoincrement," +
            "$CITY text," +
            "$WEATHER_DATA text," +
            "$DATETIME text" +
            ")"


    companion object
    {
        private const val DB_NAME = "weatherDB"
        private const val DB_VERSION = 1
    }

    fun cityWeather(city: String, weatherData: String)
    {
        val values = ContentValues()
        values.put(CITY, city)
        values.put(WEATHER_DATA, weatherData)
        values.put(DATETIME, DateFormat.getDateTimeInstance().format(Date()))
        writableDatabase.insert(TABLE, null, values)
    }

    fun cityWeatherUpdate(city: String, weatherData: String)
    {
        val values = ContentValues()
        values.put(WEATHER_DATA, weatherData)
        values.put(DATETIME, DateFormat.getDateTimeInstance().format(Date()))

        writableDatabase.update(TABLE, values, "CITY = ?", arrayOf(city))
    }

    fun deleteCity(cityName: String)
    {
        writableDatabase.delete(DB_NAME, cityName, null)
    }

    fun deleteAllCities()
    {
        writableDatabase.delete(DB_NAME, null, null)
    }

    fun getCityName(): String
    {
        lateinit var cityWeather: String
        val cursor = readableDatabase.query(TABLE, arrayOf(CITY), null,
                null, null, null, null)
        if (cursor.moveToFirst())
        {
            cursor.moveToFirst()
            cityWeather = cursor.getString(cursor.position)
            cursor.close()
        } else cityWeather = ""
        return cityWeather

    }

    fun getCityWeather(): Cursor
    {
        return readableDatabase.query(TABLE, arrayOf(ID, CITY),
                null, null, null, null, null)
    }


    override fun onCreate(db: SQLiteDatabase)
    {
        Log.d(TAG, "Create: $DATABASE_CREATE")
        db.execSQL(DATABASE_CREATE)

    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}