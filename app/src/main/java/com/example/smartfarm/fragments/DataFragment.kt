package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.imageview.ShapeableImageView

/** This is a fragment that displays the given collected data
 *  The data shown:
 * Humidity in %
 * Temperature in C
 * soil moisture 0-700 +- (700 in water, 8 +- dry ground, 650 +- wet ground just watered,588,678 little drained after watering,
 * )
 * =========== DAY ============
 * light 0-1000+- (993-1010 direct sunlight,980- => small cloud, 970 cloud & shade, 59 evening, 27-33 more evening)
 * uv 0-100 (40-100 direct sunlight,0-30 +- shade,10-20 clouds in shade, 15-35 clouds,12-13 clouds and cover & evening. 7 more evening)
 *  ######### Check UV sensor wiring #########
 *
 * ========= NIGHT ============
 *
 * */
class DataFragment(mContext: Context, data: SmartFarmData, device: SmartFarmDevice) : Fragment() {

    val mContext = mContext;
    private val data: SmartFarmData = data
    private val device: SmartFarmDevice = device
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
        getRecommendations()
        return mView;
    }

    /** This method will fetch growing recommendations according to the measured produce.
     *  recommendations regarding sunlight, water and temperature at this point.
     *  the method will check the recommended values and compare them to the measured ones,
     *  and will display a message to the user
     *  measured values:
     *
     */
    private fun getRecommendations() {
        val recommended = CodingTools.getSingleCropDetails(requireActivity(), device.produce)

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
        titleLbl.text = device.name
    }
}
