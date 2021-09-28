package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import com.example.smartfarm.MyAppClass.Constants.BT_MODE
import com.example.smartfarm.MyAppClass.Constants.QR_MODE
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.button.MaterialButton

class AddDeviceModeDialog(context: Context, resultListener: ResultListener) :
    Dialog(context) {

    private val resultListener = resultListener
    private lateinit var qrBtn: ImageView
    private lateinit var bluetoothBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_device_mode)
        initViews()
    }

    /** Init the views*/
    private fun initViews() {
        Log.d(TAG, "initViews: FirstNetworkDialog ")
        qrBtn = findViewById(R.id.addDeviceMode_IMG_qr)
        qrBtn.setOnClickListener {
            resultListener.result(true, QR_MODE)
            dismiss()
        }
        bluetoothBtn = findViewById(R.id.addDeviceMode_IMG_bluetooth)
        bluetoothBtn.setOnClickListener {
//            resultListener.result(true, BT_MODE)
//            dismiss()
            CodingTools.displayToast(context, "In Development", Toast.LENGTH_SHORT)
        }
    }
}