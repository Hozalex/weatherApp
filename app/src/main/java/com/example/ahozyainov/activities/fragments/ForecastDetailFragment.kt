package com.example.ahozyainov.activities.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.example.ahozyainov.activities.R
import kotlinx.android.synthetic.main.fragment_forecast_detail.*

open class ForecastDetailFragment : android.support.v4.app.Fragment(), View.OnClickListener {

    private lateinit var checkBoxPressure: CheckBox
    private lateinit var checkBoxTomorrowForecast: CheckBox
    private lateinit var checkBoxWeekForecast: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ForecastDetailFragment", "savedInstanceState$savedInstanceState")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var layout: View = inflater.inflate(R.layout.fragment_forecast_detail, container, false)
        checkBoxPressure = layout.findViewById(R.id.checkbox_pressure)
        checkBoxPressure.setOnClickListener(this)
        checkBoxTomorrowForecast = layout.findViewById(R.id.checkbox_tomorrow)
        checkBoxTomorrowForecast.setOnClickListener(this)
        checkBoxWeekForecast = layout.findViewById(R.id.checkbox_week)
        checkBoxWeekForecast.setOnClickListener(this)



        if (savedInstanceState != null) {
            checkbox_pressure.isChecked = savedInstanceState.getBoolean("isCheckedPressure")
            checkbox_tomorrow.isChecked = savedInstanceState.getBoolean("isCheckedTomorrowForecast")
            checkbox_week.isChecked = savedInstanceState.getBoolean("isCheckedWeekForecast")
            text_view_pressure.text = savedInstanceState.getString("tvPressure")
            text_view_tomorrow_forecast.text = savedInstanceState.getString("tvTomorrowForecast")
            text_view_week_forecast.text = savedInstanceState.getString("tvWeekForecast")

        }

        return layout
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("isCheckedPressure", checkbox_pressure.isChecked)
        outState.putBoolean("isCheckedTomorrowForecast", checkbox_tomorrow.isChecked)
        outState.putBoolean("isCheckedWeekForecast", checkbox_week.isChecked)
        outState.putString("tvPressure", text_view_pressure.text.toString())
        outState.putString("tvTomorrowForecast", text_view_tomorrow_forecast.text.toString())
        outState.putString("tvWeekForecast", text_view_week_forecast.text.toString())
        super.onSaveInstanceState(outState)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            checkbox_pressure.id -> onClickPressureCheckBox()
            checkbox_tomorrow.id -> onClickTomorrowForecastCheckBox()
            checkbox_week.id -> onClickWeekForecastCheckBox()

        }
    }

    private fun onClickPressureCheckBox() {
        if (checkbox_pressure.isChecked) {
            text_view_pressure.text = resources.getString(R.string.pressure_value)
            text_view_pressure.setBackgroundResource(R.color.colorBackground)
        } else {
            text_view_pressure.text = ""
            text_view_pressure.setBackgroundColor(0)
        }
    }

    private fun onClickTomorrowForecastCheckBox() {
        if (checkbox_tomorrow.isChecked) {
            text_view_tomorrow_forecast.text = resources.getString(R.string.tomorrow_weather)
            text_view_tomorrow_forecast.setBackgroundResource(R.color.colorBackground)
        } else {
            text_view_tomorrow_forecast.text = ""
            text_view_tomorrow_forecast.setBackgroundColor(0)
        }
    }

    private fun onClickWeekForecastCheckBox() {
        if (checkbox_week.isChecked) {
            text_view_week_forecast.text = resources.getString(R.string.week_weather)
            text_view_week_forecast.setBackgroundResource(R.color.colorBackground)
        } else {
            text_view_week_forecast.text = ""
            text_view_week_forecast.setBackgroundColor(0)
        }
    }

}