package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.interfaces.NetworkCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmDevice
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

class NewDeviceDialog(mContext: Context, did: String, listener: DeviceCallback) : Dialog(mContext) {

    private val resultListener = listener
    private val did = did
    private lateinit var submitBtn: MaterialButton
    private lateinit var nameLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var nameEdt: TextInputEditText
    private lateinit var descriptionEdt: TextInputEditText
    private lateinit var deviceId: MaterialTextView


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
        submitBtn = findViewById(R.id.newDevice_BTN_create)
        submitBtn.setOnClickListener {
            if (checkValidInput()) {
                val device = SmartFarmDevice()
                Log.d(TAG, "initViews: name: ${nameEdt.text.toString()}")
                device.did = did
                device.name = nameEdt.text.toString()
                device.description = descriptionEdt.text.toString()
                device.active = true
                resultListener.getDevice(device)
                dismiss()
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
        if (descriptionEdt.text.toString().trim().isNotEmpty()) {
            descriptionLayout.error = null
        } else {
            descriptionLayout.error = "Please enter device name"
            descriptionEdt.text?.clear()
            return false
        }
        return true
    }
}