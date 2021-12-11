package com.example.smartfarm.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.activities.MainActivity
import com.example.smartfarm.adapters.ProduceListAdapter
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.ProduceRow
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import java.text.NumberFormat

class DeviceSettingsFragment(mContext: Context, device: SmartFarmDevice, listener: DeviceCallback) :
    Fragment() {

    private val mContext = mContext
    private val device = device
    private var copyDevice = CodingTools.cloneDevice(device)
    private val listener = listener
    private lateinit var statusInfo: ShapeableImageView
    private lateinit var statusBtn: MaterialButton
    private lateinit var nameLay: TextInputLayout
    private lateinit var nameEdt: TextInputEditText
    private lateinit var descriptionLay: TextInputLayout
    private lateinit var descriptionEdt: TextInputEditText
    private lateinit var produceLay: TextInputLayout
    private lateinit var produceList: AutoCompleteTextView
    private lateinit var produceInfo: ShapeableImageView
    private lateinit var measurementInfo: ShapeableImageView
    private lateinit var intervalSlider: Slider
    private lateinit var submitBtn: MaterialButton
    private lateinit var measurementTitle: MaterialTextView


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: HomeFragment")
        (requireActivity() as MainActivity).changeToolbarTitle(getString(R.string.device_settings))
        val mView = inflater.inflate(R.layout.fragment_device_settings, container, false)
        initViews(mView)
        return mView;
    }


    /** A method that initializes the views in the fragment*/
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initViews(mView: View) {
        Log.d(MyAppClass.Constants.TAG, "initViews: HomeFragment")

        measurementTitle = mView.findViewById(R.id.deviceSettings_ILBL_measurementTitle)
        measurementTitle.text =
            "${getString(R.string.measure_interval)}:${copyDevice.measure_interval}h"
        statusInfo = mView.findViewById(R.id.deviceSettings_IMG_statusInfo)
        statusInfo.setOnClickListener {
            CodingTools.displayInfoDialog(mContext, mContext.getString(R.string.status_info))
        }
        produceInfo = mView.findViewById(R.id.deviceSettings_IMG_produceInfo)
        produceInfo.setOnClickListener {
            CodingTools.displayInfoDialog(mContext, mContext.getString(R.string.produce_info))
        }
        measurementInfo = mView.findViewById(R.id.deviceSettings_IMG_measurementInfo)
        measurementInfo.setOnClickListener {
            CodingTools.displayInfoDialog(mContext, mContext.getString(R.string.measurement_info))
        }
        statusBtn = mView.findViewById(R.id.deviceSettings_BTN_status)
        if (copyDevice.active) { // paint button green
            statusBtn.text = mContext.getString(R.string.on)
            statusBtn.setBackgroundColor(resources.getColor(R.color.green))
        } else { // paint button red
            statusBtn.text = mContext.getString(R.string.off)
            statusBtn.setBackgroundColor(resources.getColor(R.color.red))
        }
        statusBtn.setOnClickListener {
            checkDeviceOnOff()
        }
        nameLay = mView.findViewById(R.id.deviceSettings_LAY_nameLayout)
        nameEdt = mView.findViewById(R.id.deviceSettings_EDT_name)
        nameEdt.setText(device.name)
        descriptionLay = mView.findViewById(R.id.deviceSettings_LAY_descriptionLayout)
        descriptionEdt = mView.findViewById(R.id.deviceSettings_EDT_details)
        descriptionEdt.setText(device.description)
        produceLay = mView.findViewById(R.id.deviceSettings_LAY_cropTypeLayout)
        produceList = mView.findViewById(R.id.deviceSettings_LAY_cropTypeText)
        initProduceList()
        intervalSlider = mView.findViewById(R.id.deviceSettings_LAY_intervalSlider)
        intervalSlider.value = device.measure_interval.toFloat()
        intervalSlider.setLabelFormatter { value: Float ->
            val format = NumberFormat.getNumberInstance()
            format.format(value.toDouble())
        }

        intervalSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            // value = time in hours
            // time in minutes = value * 60
            var text = ""
            text = if (value < 1) { // minutes
                "${getString(R.string.measure_interval)}:${String.format("%.0f", value * 60)}m"
            } else {
                "${getString(R.string.measure_interval)}:${String.format("%.2f", value)}h"
            }
            measurementTitle.text = text
        }

        submitBtn = mView.findViewById(R.id.deviceSettings_BTN_submit)
        submitBtn.setOnClickListener {
            checkUserInput()
        }
    }

    /** This method will be executed once the submit button has been pressed, and
     * if all the checks pass, it will send the updated device back to the caller
     */
    private fun checkUserInput() {
        Log.d(TAG, "checkUserInput: ")
        if (nameEdt.text!!.isEmpty()) { // check name
            nameLay.error = mContext.getString(R.string.please_enter_device_name)
            return
        }
        if (produceList.text.isEmpty()) { // check produce
            produceLay.error = mContext.resources.getString(R.string.produce_error)
            return
        }

        copyDevice.name = nameEdt.text.toString()
        copyDevice.description = descriptionEdt.text.toString()
        copyDevice.measure_interval = intervalSlider.value.toDouble()


        // Send the device back to caller
        listener.getDevice(copyDevice)

        Log.d(TAG, "checkUserInput: Popping stack")
        parentFragmentManager.popBackStack()// pop the fragment
    }


    /** This method will populate the produce list and select the current produce*/
    private fun initProduceList() {
        Log.d(TAG, "initProduceList: ")
        val plainNames = mContext.resources.getStringArray(R.array.produce)
        val adapter =
            ProduceListAdapter(
                mContext,
                R.layout.row_produce_item,
                CodingTools.getCropsFromResources(mContext)
            )
        produceList.setAdapter(adapter)
        produceList.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                copyDevice.produce = plainNames[id.toInt()]
            }
        }
        // Search for the item in the list
        for ((i, item) in plainNames.withIndex()) {
            if (item.lowercase() == copyDevice.produce.lowercase()) {
                Log.d(TAG, "initProduceList: Found = $item")
                produceList.setText(item)
            }
        }
    }

    /** This method will be executed once the device on/off button is clicked.
     * the method will compare the 'active' variable and update UI according to it
     */
    private fun checkDeviceOnOff() {
        Log.d(TAG, "checkDeviceOnOff: ")
        if (copyDevice.active) {// if the device on turn it off
            copyDevice.active = false // turn off device
            statusBtn.text = mContext.getString(R.string.off)
            statusBtn.setBackgroundColor(resources.getColor(R.color.red))
        } else {
            copyDevice.active = true // turn on device
            statusBtn.text = mContext.getString(R.string.on)
            statusBtn.setBackgroundColor(resources.getColor(R.color.green))
        }
    }
}