package com.example.ahozyainov.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import com.example.ahozyainov.activities.fragments.WeatherForecastFragment
import com.example.ahozyainov.adapters.CityAdapter
import com.example.ahozyainov.common.IntentHelper
import com.example.ahozyainov.models.Cities
import com.example.ahozyainov.models.JSONData
import com.example.ahozyainov.models.WeatherDatabaseHelper
import com.example.ahozyainov.utils.JSONGetter
import com.example.ahozyainov.utils.MyLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{
    private val INITIAL_PERM = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    private var sharedText = ""
    private val mySettings = "mySettings"
    private val sendRequestCode = 1
    private lateinit var settings: SharedPreferences
    private var twoPane: Boolean = false
    private lateinit var citiesArrayList: ArrayList<Cities>
    private var cityListFileName = "cityList"
    private var avatarFileName = "default_avatar"
    private lateinit var internalPath: String
    private lateinit var externalPath: String
    private var getAvatarUrl: String = "https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/18194920_1733866159958632_3331095931294444894_n.jpg?_nc_cat=0&oh=57f7974c221699e603acfb99e8f7d5a1&oe=5BA8EBBD"
    private lateinit var popupMenu: PopupMenu
    private var isPressureChecked: Boolean = false
    private var isHumidityChecked: Boolean = false
    private var isWindChecked: Boolean = false
    private lateinit var avatarImageView: ImageView
    private lateinit var avatarData: AvatarData
    private val TAG = "mainActivity"
    private lateinit var jsonGetter: JSONGetter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(INITIAL_PERM, 1337)
        }
        twoPane = findViewById<View>(R.id.flRightContainer) != null
        settings = getSharedPreferences(mySettings, Context.MODE_PRIVATE)
        citiesArrayList = ArrayList(10)
        avatarImageView = nav_view.getHeaderView(0).findViewById(R.id.ivHeader)
        internalPath = filesDir.toString()
        externalPath = Environment.getExternalStorageDirectory().absolutePath.toString()
        avatarData = AvatarData(avatarImageView, internalPath, externalPath, avatarFileName)
        jsonGetter = JSONGetter(this)
        rvCities.setHasFixedSize(true)
        rvCities.layoutManager = LinearLayoutManager(this)
        registerForContextMenu(rvCities)
        readCityList(internalPath + cityListFileName)
        readAvatarInternal(internalPath + avatarFileName)
        if (savedInstanceState != null)
        {
            setSavedInstanceCity(savedInstanceState)
        }

        getSettings()
        setSupportActionBar(toolbar)
        initActionBar()
        nav_view.setNavigationItemSelectedListener(this)
        addAdapter(savedInstanceState)
        initPopUpMenu(popButton)

        if (citiesArrayList.isEmpty())
        {
            getNewCityName()
        }

    }

    private fun getNewCityName()
    {
        jsonGetter.execute()
        Log.d(TAG, "new city ${jsonGetter.get().cityName}")
        citiesArrayList.add(0, Cities(jsonGetter.get().cityName))
        addAdapter(savedInstanceState = Bundle())
    }

    private fun getSettings()
    {
        isPressureChecked = settings.getBoolean(IntentHelper.EXTRA_CHECKBOX_PRESSURE, true)
        isHumidityChecked = settings.getBoolean(IntentHelper.EXTRA_CHECKBOX_HUMIDITY, true)
        isWindChecked = settings.getBoolean(IntentHelper.EXTRA_CHECKBOX_WIND, true)
    }

    private fun setSavedInstanceCity(savedInstanceState: Bundle)
    {
        var cityList = savedInstanceState.getStringArrayList(IntentHelper.EXTRA_ARRAY_CITIES)
        for (cityName in cityList)
        {
            citiesArrayList.add(Cities(cityName))
        }
    }

    private fun initActionBar()
    {
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.open, R.string.close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.menu_info ->
            {
                val toast = Toast.makeText(applicationContext, R.string.attention_info, Toast.LENGTH_LONG)
                toast.duration = Toast.LENGTH_LONG
                toast.show()
                return true
            }
            R.id.menu_about ->
            {
                val toast = Toast.makeText(applicationContext, R.string.about_text, Toast.LENGTH_LONG)
                toast.duration = Toast.LENGTH_LONG
                toast.show()
                return true
            }
            R.id.menu_feedback ->
            {
                intent = Intent(Intent.ACTION_SEND)
                intent.type = "message/email"
                intent.putExtra(Intent.EXTRA_EMAIL, getText(R.string.feedback_mail_to))
                intent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.feedback_mail_subject))

                if (intent.resolveActivity(packageManager) != null)
                {
                    startActivity(intent)
                }

            }

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true

    }

    private fun initPopUpMenu(it: View)
    {
        popupMenu = PopupMenu(this, it)
        menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popButton.setOnClickListener {

            popupMenu.setOnMenuItemClickListener {

                when (it.itemId)
                {
                    R.id.pressure_menu_checkbox ->
                    {
                        it.isChecked = !it.isChecked
                        isPressureChecked = it.isChecked
                        false
                    }
                    R.id.humidity_menu_checkbox ->
                    {
                        it.isChecked = !it.isChecked
                        isHumidityChecked = it.isChecked
                        false
                    }
                    R.id.wind_menu_checkbox ->
                    {
                        it.isChecked = !it.isChecked
                        isWindChecked = it.isChecked
                        false
                    }
                    else ->
                    {
                        super.onOptionsItemSelected(it)
                    }
                }
            }
            popupMenu.show()

        }
    }

    private fun addAdapter(savedInstanceState: Bundle?)
    {
        rvCities.adapter = CityAdapter(citiesArrayList, CityAdapter.OnCityClickListener { cityPosition ->
            run {
                if (!twoPane)
                {
                    intent = Intent(this, WeatherActivity::class.java)
                    intent.putExtra(IntentHelper.EXTRA_CITY_NAME, citiesArrayList[cityPosition].name)
                    intent.putExtra(IntentHelper.EXTRA_CHECKBOX_PRESSURE, isPressureChecked)
                    intent.putExtra(IntentHelper.EXTRA_CHECKBOX_HUMIDITY, isHumidityChecked)
                    intent.putExtra(IntentHelper.EXTRA_CHECKBOX_WIND, isWindChecked)
                    startActivityForResult(intent, sendRequestCode)
                } else
                {
                    showWeatherForecastFragment(cityPosition)
                    popButton.hide()
                }
            }
        })

        if (twoPane && savedInstanceState == null)
        {
            showWeatherForecastFragment(0)
            popButton.hide()
        }

        rvCities.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        when (item!!.itemId)
        {
            R.id.menu_add ->
            {
                addCity()
                return true
            }
            R.id.menu_clear ->
            {
                clearCities()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean
    {

        deleteCity(item.itemId)
        return super.onContextItemSelected(item)
    }


    private fun deleteCity(itemId: Int)
    {
        //TODO
//        val databaseHelper = WeatherDatabaseHelper(context = this)
//        databaseHelper.deleteCity(cityName)
//        citiesArrayList.removeAt(itemId)
//        addAdapter(savedInstanceState = Bundle())
    }

    private fun clearCities()
    {
        val databaseHelper = WeatherDatabaseHelper(context = this)
        citiesArrayList.clear()
        databaseHelper.deleteAllCities()
        deleteCityList(internalPath + cityListFileName)
        addAdapter(savedInstanceState = Bundle())
    }

    private fun addCity()
    {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.input_city)
        val inputText = EditText(this)
        alert.setView(inputText)
        alert.setPositiveButton("Ok") { dialogInterface, i ->
            if (inputText.text.isNotEmpty())
            {
                citiesArrayList.add(Cities(inputText.text.toString()))
                addAdapter(savedInstanceState = Bundle())
            }
        }
        alert.show()

    }

    private fun showWeatherForecastFragment(cityPosition: Int)
    {
        supportFragmentManager.beginTransaction().replace(R.id.flRightContainer,
                WeatherForecastFragment.newInstance(cityPosition))
                .commit()
    }

    override fun onSaveInstanceState(outState: Bundle?)
    {

        outState?.putString(IntentHelper.EXTRA_SHARED_WEATHER, sharedText)
        outState?.putStringArrayList(IntentHelper.EXTRA_ARRAY_CITIES, getArrayListCities())
        super.onSaveInstanceState(outState)
    }

    private fun getArrayListCities(): ArrayList<String>
    {
        var cityListName: ArrayList<String> = ArrayList()
        for (city in citiesArrayList)
        {
            cityListName.add(city.name)
        }

        return cityListName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == sendRequestCode)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                sharedText = data!!.getStringExtra(IntentHelper.EXTRA_SHARED_WEATHER)
                text_view_main.text = sharedText
            }
        }
    }

    override fun onStop()
    {

        saveCityList(internalPath + cityListFileName)
        settings.edit().putBoolean(IntentHelper.EXTRA_CHECKBOX_PRESSURE, isPressureChecked).apply()
        settings.edit().putBoolean(IntentHelper.EXTRA_CHECKBOX_HUMIDITY, isHumidityChecked).apply()
        settings.edit().putBoolean(IntentHelper.EXTRA_CHECKBOX_WIND, isWindChecked).apply()

        super.onStop()
    }

    private fun saveCityList(path: String)
    {
        var cityList: File

        try
        {
            cityList = File(path)
            val fileOutputStream: FileOutputStream
            val objectOutputStream: ObjectOutputStream

            if (!cityList.exists())
            {
                cityList.createNewFile()
            }

            fileOutputStream = FileOutputStream(cityList, false)
            objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(citiesArrayList)

            fileOutputStream.close()
            objectOutputStream.close()
        } catch (e: Exception)
        {
            e.printStackTrace()
            Log.d("CityListFile", e.toString())
        }


    }

    private fun deleteCityList(path: String)
    {
        var cityList: File
        if (citiesArrayList.isNotEmpty())
            try
            {
                cityList = File(path)
                cityList.delete()
            } catch (e: Exception)
            {
                e.printStackTrace()
                Log.d("CityListFile", e.toString())
            }

    }

    private fun readCityList(path: String)
    {
        var fileInputStream: FileInputStream
        var objectInputStream: ObjectInputStream

        try
        {
            fileInputStream = FileInputStream(path)
            objectInputStream = ObjectInputStream(fileInputStream)
            citiesArrayList = objectInputStream.readObject() as ArrayList<Cities>
            addAdapter(savedInstanceState = Bundle())

            fileInputStream.close()
            objectInputStream.close()
        } catch (e: Exception)
        {
            e.printStackTrace()
        }

    }


    private fun readAvatarInternal(path: String)
    {
        val fileInputStream: FileInputStream
        val objectInputStream: ObjectInputStream
        var bitmap: Bitmap? = null
        try
        {
            fileInputStream = FileInputStream(path)
            objectInputStream = ObjectInputStream(fileInputStream)
            bitmap = getBitmapFromByteArray(objectInputStream.readObject() as ByteArray)
            fileInputStream.close()
            objectInputStream.close()
        } catch (e: Exception)
        {
            e.printStackTrace()

        }
        if (bitmap == null)
        {
            avatarImageView.setImageBitmap(avatarData.execute(getAvatarUrl).get())
        } else
        {
            avatarImageView.setImageBitmap(bitmap)
        }

    }

    private fun getBitmapFromByteArray(bytes: ByteArray): Bitmap?
    {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }


    private class AvatarData(var imageView: ImageView, var internalPath: String, var externalPath: String,
                             var fileName: String) : AsyncTask<String, Void, Bitmap>(), Serializable
    {

        var bmIcon: Bitmap? = null

        override fun doInBackground(vararg p0: String?): Bitmap?
        {
            val url = URL(p0[0])
            var icon: Bitmap? = null
            try
            {
                icon = BitmapFactory.decodeStream(url.openConnection().getInputStream())

            } catch (e: Exception)
            {
                Log.d("Error", e.message)
                e.printStackTrace()
            }
            bmIcon = icon
            return icon
        }

        override fun onPostExecute(result: Bitmap?)
        {
            saveAvatarInternal()
            saveAvatarExternal()
        }

        private fun saveAvatarExternal()
        {
            val avatarExternalFilePath: File
            val avatarExternalFile: File
            val fileOutputStream: FileOutputStream
            val objectOutputStream: ObjectOutputStream

            avatarExternalFilePath = File(externalPath)
            if (!avatarExternalFilePath.exists())
            {
                avatarExternalFilePath.mkdirs()
            }
            avatarExternalFile = File(avatarExternalFilePath, fileName)

            try
            {
                fileOutputStream = FileOutputStream(avatarExternalFile, false)
                objectOutputStream = ObjectOutputStream(fileOutputStream)
                objectOutputStream.writeObject(getByteArrayFromDrawable(imageView.drawable))
                fileOutputStream.close()
                objectOutputStream.close()

            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }

        private fun saveAvatarInternal()
        {
            val avatarInternalFile: File

            try
            {
                avatarInternalFile = File(internalPath + fileName)

                val fileOutputStream: FileOutputStream
                val objectOutputStream: ObjectOutputStream

                if (!avatarInternalFile.exists())
                {
                    avatarInternalFile.createNewFile()
                }

                fileOutputStream = FileOutputStream(avatarInternalFile, false)
                objectOutputStream = ObjectOutputStream(fileOutputStream)
                objectOutputStream.writeObject(getByteArrayFromDrawable(imageView.drawable))
                fileOutputStream.close()
                objectOutputStream.close()


            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }

        private fun getByteArrayFromDrawable(drawable: Drawable): ByteArray?
        {
            var bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
            var bytes: ByteArray? = null
            try
            {
                var stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                bytes = stream.toByteArray()
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
            return bytes

        }

    }


}


