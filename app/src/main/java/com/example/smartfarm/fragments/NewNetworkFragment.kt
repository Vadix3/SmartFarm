package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.models.SmartFarmNetwork
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.ArrayAdapter


class NewNetworkFragment(mContext: Context) : Fragment() {

    val mContext = mContext

    private lateinit var nameBox: TextInputEditText
    private lateinit var idBox: TextInputEditText
    private lateinit var nameLayout: TextInputLayout
    private lateinit var idLayout: TextInputLayout
    private lateinit var deviceRecycler: ListView
    private lateinit var createBtn: MaterialButton
    private lateinit var addDeviceBtn: MaterialButton
    private val deviceList = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: new network fragment")
        val mView = inflater.inflate(R.layout.dialog_new_network, container, false)
        initViews(mView)
        return mView;
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: New network")
        nameBox = mView.findViewById(R.id.newNetwork_EDT_name)
        idBox = mView.findViewById(R.id.newNetwork_EDT_deviceId)
        nameLayout = mView.findViewById(R.id.newNetwork_LAY_nameLayout)
        idLayout = mView.findViewById(R.id.newNetwork_LAY_deviceId)
        deviceRecycler = mView.findViewById(R.id.newNetwork_LST_devices)
        val adapter =
            ArrayAdapter(mContext, android.R.layout.simple_list_item_1, deviceList)
        deviceRecycler.adapter = adapter

        addDeviceBtn = mView.findViewById(R.id.newNetwork_BTN_addDevice)
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
        createBtn = mView.findViewById(R.id.newNetwork_BTN_create)
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
        Log.d(TAG, "addNewNetwork: ")
        val data = SmartFarmNetwork()
        data.owner = MyAppClass.Constants.userEmail
        data.name = nameBox.text.toString()
        data.devices = deviceList
        Log.d(TAG, "addNewNetwork: Adding data: $data")
    }

}

