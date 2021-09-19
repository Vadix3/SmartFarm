package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.interfaces.MeasurementCallback
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView

/** This is a fragment that displays the given collected data*/
class DataFragment(mContext: Context, data: SmartFarmData) : Fragment() {

    val mContext = mContext;
    private val data: SmartFarmData = data
    private lateinit var dataController: DataController

    // Textviews - date, time, humidity, temp, soil, light, uv, title
    private lateinit var dateLbl: TextView
    private lateinit var timeLbl: TextView
    private lateinit var humidityLbl: TextView
    private lateinit var tempLbl: TextView
    private lateinit var soilLbl: TextView
    private lateinit var lightLbl: TextView
    private lateinit var uvLbl: TextView
    private lateinit var titleLbl: TextView

    // Icons
    private lateinit var dateImg: ShapeableImageView
    private lateinit var timeImg: ShapeableImageView
    private lateinit var humidityImg: ShapeableImageView
    private lateinit var tempImg: ShapeableImageView
    private lateinit var soilImg: ShapeableImageView
    private lateinit var lightImg: ShapeableImageView
    private lateinit var uvImg: ShapeableImageView
    private lateinit var titleImg: ShapeableImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: Device details fragment")
        val mView = inflater.inflate(R.layout.fragment_data, container, false)
        dataController = DataController(mContext)
        initViews(mView)
        return mView;
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")

        //labels
        dateLbl = mView.findViewById(R.id.data_LBL_date)
        timeLbl = mView.findViewById(R.id.data_LBL_time)
        humidityLbl = mView.findViewById(R.id.data_LBL_humidity)
        tempLbl = mView.findViewById(R.id.data_LBL_temperature)
        soilLbl = mView.findViewById(R.id.data_LBL_soil)
        lightLbl = mView.findViewById(R.id.data_LBL_light)
        uvLbl = mView.findViewById(R.id.data_LBL_uv)
        titleLbl = mView.findViewById(R.id.data_LBL_title)

        //images
        dateImg = mView.findViewById(R.id.data_IMG_date)
        timeImg = mView.findViewById(R.id.data_IMG_time)
        humidityImg = mView.findViewById(R.id.data_IMG_humidity)
        tempImg = mView.findViewById(R.id.data_IMG_temperature)
        soilImg = mView.findViewById(R.id.data_IMG_soil)
        lightImg = mView.findViewById(R.id.data_IMG_light)
        uvImg = mView.findViewById(R.id.data_IMG_uv)

        //inject values
        dateLbl.text = resources.getString(R.string.date) + ": " + data.date
        timeLbl.text = resources.getString(R.string.time) + ": " + data.time
        humidityLbl.text =
            resources.getString(R.string.humidity) + ": " + String.format("%.2f", data.humidity)
        tempLbl.text =
            resources.getString(R.string.temperature) + ": " + String.format(
                "%.2f",
                data.temperature
            )
        soilLbl.text = resources.getString(R.string.soil_moisture) + ": " + data.soil.toString()
        lightLbl.text = resources.getString(R.string.light_exposure) + ": " + data.light.toString()
        uvLbl.text = resources.getString(R.string.uv_exposure) + ": " + data.uv.toString()
        titleLbl.text = data.device
    }
}
