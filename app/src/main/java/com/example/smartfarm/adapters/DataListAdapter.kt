package com.example.smartfarm.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.WeatherReport
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.sql.Date
import java.sql.Timestamp
import kotlin.math.roundToInt

class DataListAdapter(
    context: Context,
    private val dataSet: ArrayList<SmartFarmData>,
) :
    RecyclerView.Adapter<DataListAdapter.ViewHolder>() {

    private var mContext = context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        val dateImg: ImageView = v.findViewById(R.id.dataRow_IMG_date)
        val dateLbl: TextView = v.findViewById(R.id.dataRow_LBL_date)
        val timeImg: ImageView = v.findViewById(R.id.dataRow_IMG_time)
        val timeLbl: TextView = v.findViewById(R.id.dataRow_LBL_time)
        val humidityImg: ImageView = v.findViewById(R.id.dataRow_IMG_humidity)
        val humidityLbl: TextView = v.findViewById(R.id.dataRow_LBL_humidity)
        val temperatureImg: ImageView = v.findViewById(R.id.dataRow_IMG_temperature)
        val temperatureLbl: TextView = v.findViewById(R.id.dataRow_LBL_temperature)
        val soilImg: ImageView = v.findViewById(R.id.dataRow_IMG_soil)
        val soilLbl: TextView = v.findViewById(R.id.dataRow_LBL_soil)
        val lightImg: ImageView = v.findViewById(R.id.dataRow_IMG_light)
        val lightLbl: TextView = v.findViewById(R.id.dataRow_LBL_light)
        val uvImg: ImageView = v.findViewById(R.id.dataRow_IMG_uv)
        val uvLbl: TextView = v.findViewById(R.id.dataRow_LBL_uv)
        val mainLayout: LinearLayout = v.findViewById(R.id.dataRow_LAY_mainLayout)
        val noDataLbl: MaterialTextView = v.findViewById(R.id.dataRow_LBL_noData)

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_data_recycler, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(MyAppClass.Constants.TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        if (temp.date != "") {
            //inject values
            viewHolder.dateLbl.text = mContext.getString(R.string.date) + ": " + temp.date
            viewHolder.timeLbl.text = mContext.getString(R.string.time) + ": " + temp.time
            if (temp.humidity != -999.0) {
                viewHolder.humidityLbl.text =
                    mContext.getString(R.string.humidity) + ": " + temp.humidity.roundToInt() + "%"
            } else {
                viewHolder.humidityLbl.text =
                    mContext.getString(R.string.no_data)
            }
            if (temp.temperature != -999.0) {
                viewHolder.temperatureLbl.text =
                    mContext.getString(R.string.temperature) + ": " + temp.temperature.roundToInt() + "°C"
            } else {
                viewHolder.temperatureLbl.text =
                    mContext.getString(R.string.no_data)
            }
            if (temp.soil != -999) {
                viewHolder.soilLbl.text =
                    mContext.getString(R.string.soil_moisture) + ": " + temp.soil.toString()
            } else {
                viewHolder.soilLbl.text = mContext.getString(R.string.no_data)
            }
            if (temp.light != -999) {
                viewHolder.lightLbl.text =
                    mContext.getString(R.string.light_exposure) + ": " + temp.light.toString()
            } else {
                viewHolder.lightLbl.text = mContext.getString(R.string.no_data)
            }
            if (temp.uv != -999) {
                viewHolder.uvLbl.text =
                    mContext.getString(R.string.uv_exposure) + ": " + temp.uv.toString()
            } else {
                viewHolder.uvLbl.text = mContext.getString(R.string.no_data)
            }
        } else {
            Log.d(TAG, "onBindViewHolder: No data")
            viewHolder.mainLayout.visibility = LinearLayout.GONE
            viewHolder.noDataLbl.visibility = LinearLayout.VISIBLE
        }
    }

    override fun getItemCount() = dataSet.size
}