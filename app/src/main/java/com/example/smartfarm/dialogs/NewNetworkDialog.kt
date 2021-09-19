package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.NetworkCallback
import com.example.smartfarm.models.SmartFarmNetwork
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class NewNetworkDialog(mContext: Context, listener: NetworkCallback) : Dialog(mContext) {

    private val mContext = mContext
    private val listener = listener

    private lateinit var nameBox: TextInputEditText
    private lateinit var idBox: TextInputEditText
    private lateinit var nameLayout: TextInputLayout
    private lateinit var idLayout: TextInputLayout
    private lateinit var deviceRecycler: ListView
    private lateinit var createBtn: MaterialButton
    private lateinit var addDeviceBtn: MaterialButton
    private val deviceList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_new_network)
        initViews()
    }

    private fun initViews() {
        Log.d(MyAppClass.Constants.TAG, "initViews: New network")
        nameBox = findViewById(R.id.newNetwork_EDT_name)
        idBox = findViewById(R.id.newNetwork_EDT_deviceId)
        nameLayout = findViewById(R.id.newNetwork_LAY_nameLayout)
        idLayout = findViewById(R.id.newNetwork_LAY_deviceId)
        deviceRecycler = findViewById(R.id.newNetwork_LST_devices)
        val adapter =
            ArrayAdapter(mContext, android.R.layout.simple_list_item_1, deviceList)
        deviceRecycler.adapter = adapter

        addDeviceBtn = findViewById(R.id.newNetwork_BTN_addDevice)
        addDeviceBtn.setOnClickListener {
            if (idBox.text.toString().trim().isNotEmpty()) {
                deviceList.add(idBox.text.toString()) // add the device id to the list
                adapter.notifyDataSetChanged() // refresh the devices list
                idBox.text?.clear()
                idLayout.error = null
            } else {
                idLayout.error = "Please enter device ID"
            }
        }
        createBtn = findViewById(R.id.newNetwork_BTN_create)
        createBtn.setOnClickListener {
            if (deviceList.size == 0) {
                idLayout.error = "Please add a device"
            } else if (nameBox.text.toString().trim().isEmpty()) {
                nameLayout.error = "Please enter network name"
            } else {
                addNewNetwork()
            }
        }
    }

    /** This function will upload the new network object to the cloud*/
    private fun addNewNetwork() {
        Log.d(MyAppClass.Constants.TAG, "addNewNetwork: ")
        val data = SmartFarmNetwork()
        data.owner = MyAppClass.Constants.userEmail
        data.name = nameBox.text.toString()
        data.devices = deviceList
        listener.getNetwork(data)
        dismiss()
    }

}