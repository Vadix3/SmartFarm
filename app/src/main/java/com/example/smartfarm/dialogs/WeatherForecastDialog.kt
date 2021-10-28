package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.R
import com.example.smartfarm.adapters.WeatherListAdapter
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.WeatherReport
import com.google.android.material.button.MaterialButton

class WeatherForecastDialog (context: Context, weatherList: ArrayList<WeatherReport>) :
    Dialog(context) {

    private val weatherList = weatherList
    private lateinit var weatherRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_forecast)
        initViews()
    }

    /** Init the views*/
    private fun initViews() {
        Log.d(MyAppClass.Constants.TAG, "initViews: FirstNetworkDialog ")
        weatherRecycler=findViewById(R.id.forecast_LST_recyclerview)

        //testing
        weatherRecycler.adapter=WeatherListAdapter(context,weatherList)

    }
}