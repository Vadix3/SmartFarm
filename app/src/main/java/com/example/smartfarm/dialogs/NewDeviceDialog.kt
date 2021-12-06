package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.adapters.ProduceListAdapter
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.models.ProduceRow
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList

class NewDeviceDialog(mContext: Context, did: String, listener: DeviceCallback) : Dialog(mContext) {

    private val device = SmartFarmDevice()
    private val mContext = mContext
    private val did = did
    private val resultListener = listener
    private val plainNames = context.resources.getStringArray(R.array.produce)
    private lateinit var submitBtn: MaterialButton
    private lateinit var nameLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var nameEdt: TextInputEditText
    private lateinit var descriptionEdt: TextInputEditText
    private lateinit var deviceId: MaterialTextView
    private lateinit var cropTypeLayout: TextInputLayout
    private lateinit var cropTypeList: AutoCompleteTextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_new_device)
        initViews()
    }

    private fun initViews() {
        Log.d(TAG, "initViews: ")
        nameLayout = findViewById(R.id.newDevice_LAY_nameLayout)
        descriptionLayout = findViewById(R.id.newDevice_LAY_descriptionLayout)
        nameEdt = findViewById(R.id.newDevice_EDT_name)
        descriptionEdt = findViewById(R.id.newDevice_EDT_details)
        deviceId = findViewById(R.id.newDevice_LAY_deviceId)
        deviceId.text = did

        cropTypeLayout = findViewById(R.id.newDevice_LAY_cropTypeLayout)
        cropTypeList = findViewById(R.id.newDevice_LAY_cropTypeText)
        val adapter =
            ProduceListAdapter(
                context,
                R.layout.row_produce_item,
                CodingTools.getCropsFromResources(mContext)
            )
        cropTypeList.setAdapter(adapter)
        cropTypeList.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                device.produce = plainNames[id.toInt()]
            }

        }
        submitBtn = findViewById(R.id.newDevice_BTN_create)
        submitBtn.setOnClickListener {
            if (checkValidInput()) {
                Log.d(TAG, "initViews: name: ${nameEdt.text.toString()}")
                device.did = did
                device.name = nameEdt.text.toString()
                device.description = descriptionEdt.text.toString()
                device.active = true
                Log.d(TAG, "initViews: sending device: $device")
//                resultListener.getDevice(device)
//                dismiss()
            }
        }
    }


    private fun checkValidInput(): Boolean {
        Log.d(TAG, "checkValidInput: ")
        if (nameEdt.text.toString().trim().isNotEmpty()) {
            nameLayout.error = null
        } else {
            nameLayout.error = "Please enter device name"
            nameEdt.text?.clear()
            return false
        }
        if (cropTypeList.text.isEmpty()) {
            cropTypeLayout.error = context.resources.getString(R.string.produce_error)
        }
        return true
    }
}