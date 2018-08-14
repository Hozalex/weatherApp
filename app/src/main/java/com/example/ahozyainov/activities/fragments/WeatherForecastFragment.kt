package com.example.ahozyainov.activities.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ahozyainov.activities.R
import com.example.ahozyainov.common.IntentHelper
import com.example.ahozyainov.models.Cities
import kotlinx.android.synthetic.main.activity_weather.*


class WeatherForecastFragment : Fragment()
{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        var fragmentManager: FragmentManager = childFragmentManager
        var forecastDetailFragment = fragmentManager.findFragmentByTag("FORECAST_DETAIL_FRAGMENT_TAG")
        if (forecastDetailFragment == null)
        {
            var fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            forecastDetailFragment = ForecastDetailFragment()
            fragmentTransaction.replace(R.id.forecast_detail_container, forecastDetailFragment, "FORECAST_DETAIL_FRAGMENT_TAG")
            fragmentTransaction.commit()
        }
        return inflater.inflate(R.layout.fragment_weather_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        if (arguments == null) return
        var cities = Cities.getAllCities(context)
        var city: Cities = cities[arguments!!.getInt(IntentHelper.EXTRA_CITY_ID)]
        text_view_city.text = city.name
        text_view_weather.setText(city.descriptionId)
        image_weather_activity.setImageResource(city.imageId)

        share_button.setOnClickListener {
            var shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_TEXT, text_view_city.text.toString() + ": " +
                    text_view_weather.text.toString())
            shareIntent.type = "text/plain"
            startActivity(shareIntent)

        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance(cityId: Int) =
                WeatherForecastFragment().apply {
                    arguments = Bundle().apply {
                        putInt(IntentHelper.EXTRA_CITY_ID, cityId)
                    }
                }
    }


}
