package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.models.SmartFarmNetwork
import com.example.smartfarm.models.WeatherReport
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.bumptech.glide.Glide
import android.graphics.drawable.Drawable
import android.opengl.Visibility

import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.smartfarm.dialogs.WeatherForecastDialog
import com.example.smartfarm.utils.CodingTools


class WeatherPreviewFragment(
    mContext: Context,
    currentWeather: WeatherReport,
    forecastList: ArrayList<WeatherReport>,
) : Fragment() {

    private val mContext = mContext;
    private var currentWeather = currentWeather
    private var forecastList = forecastList

    private lateinit var cardLayout: MaterialCardView // all the card
    private lateinit var reloadImage: ImageView
    private lateinit var iconImg: ImageView  // imageview for the weather icon
    private lateinit var tempLbl: TextView // temperature label
    private lateinit var descriptionLbl: TextView // weather description label


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: WeatherPreviewFragment")
        val mView = inflater.inflate(R.layout.fragment_weather_preview, container, false)
        initViews(mView)
        updateUIWithData()
        return mView;
    }

    /** This method will update the UI with the current weather data*/
    private fun updateUIWithData() {
        Log.d(TAG, "updateUIWithData: ")

        //Temperature
        tempLbl.text = "${currentWeather.temp.toInt()}°C"
        //Description
        descriptionLbl.text = currentWeather.weather_description
        //Icon
        val url = getString(R.string.base_icon_link) + currentWeather.icon + ".png"
        Glide.with(this).load(url).placeholder(CodingTools.getCircularDrawable(mContext))
            .error(R.drawable.ic_baseline_error_outline_24).into(iconImg)

    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")
        cardLayout = mView.findViewById(R.id.weatherfragment_LAY_cardview)
        iconImg = mView.findViewById(R.id.weatherfragment_IMG_weatherIcon)
        tempLbl = mView.findViewById(R.id.weatherfragment_LBL_weatherTemp)
        descriptionLbl = mView.findViewById(R.id.weatherfragment_LBL_weatherDescription)
        reloadImage = mView.findViewById(R.id.weatherfragment_IMG_loading)
        reloadImage.setImageDrawable(CodingTools.getCircularDrawable(mContext))
        cardLayout.setOnClickListener {
            openWeatherForecast()
        }
    }

    /** This method will open the weekly weather forecast dialog*/
    private fun openWeatherForecast() {
        Log.d(TAG, "openWeatherForecast: ")
        CodingTools.openDialog(
            mContext,
            WeatherForecastDialog(mContext, forecastList),
            MyAppClass.Constants.WRAP_CONTENT,
            (mContext.resources.displayMetrics.widthPixels * 0.9).toInt(),
            0.9f
        )
    }
}