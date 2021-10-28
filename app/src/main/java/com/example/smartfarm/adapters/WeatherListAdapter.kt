package com.example.smartfarm.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.WeatherReport
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.card.MaterialCardView
import java.sql.Date
import java.sql.Timestamp

class WeatherListAdapter(
    context: Context,
    private val dataSet: ArrayList<WeatherReport>,
) :
    RecyclerView.Adapter<WeatherListAdapter.ViewHolder>() {

    private var mContext = context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        val iconImg: ImageView = v.findViewById(R.id.weatherRow_IMG_icon)
        val dayLbl: TextView = v.findViewById(R.id.weatherRow_LBL_day)
        val descriptionLbl: TextView = v.findViewById(R.id.weatherRow_LBL_description)
        val realTempLbl: TextView = v.findViewById(R.id.weatherRow_LBL_realTemp)
        val feelLikeLbl: TextView = v.findViewById(R.id.weatherRow_LBL_feelsLike)
        val humidityLbl: TextView = v.findViewById(R.id.weatherRow_LBL_humidity)
        val windSpeedLbl: TextView = v.findViewById(R.id.weatherRow_LBL_windSpeed)
        val sunriseLbl: TextView = v.findViewById(R.id.weatherRow_LBL_sunrise)
        val sunsetLbl: TextView = v.findViewById(R.id.weatherRow_LBL_sunset)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_forecast_recycler, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(MyAppClass.Constants.TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        // load image
        val url = mContext.getString(R.string.base_icon_link) + temp.icon + ".png"

        val circularProgressDrawable = CodingTools.getCircularDrawable(mContext)

        Glide.with(mContext).load(url).placeholder(circularProgressDrawable)
            .error(R.drawable.ic_baseline_error_outline_24).into(viewHolder.iconImg)

        viewHolder.dayLbl.text = CodingTools.getDayOfWeek(temp.date)
        viewHolder.descriptionLbl.text = temp.weather_description
        viewHolder.realTempLbl.text = "Temperature: " + (temp.temp.toInt().toString() + "°C")
        viewHolder.feelLikeLbl.text = "Feels like: " + (temp.feels_like.toInt().toString() + "°C")
        viewHolder.humidityLbl.text = "Humidity: " + temp.humidity.toString() + "%"
        viewHolder.windSpeedLbl.text =
            "Wind speed: " + (temp.wind_speed * 3.6).toInt().toString() + "km/h"
        viewHolder.sunriseLbl.text = "Sunrise: " + CodingTools.getHourAndMinute(temp.sunrise)
        viewHolder.sunsetLbl.text = "Sunset: " + CodingTools.getHourAndMinute(temp.sunset)
    }

    override fun getItemCount() = dataSet.size
}