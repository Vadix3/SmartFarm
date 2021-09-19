package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.adapters.DeviceListAdapter
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.interfaces.DeviceListCallback
import com.example.smartfarm.interfaces.MeasurementCallback
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NetworkDetailsFragment(mContext: Context, network: SmartFarmNetwork) : Fragment() {

    val mContext = mContext;
    private val network: SmartFarmNetwork = network
    private lateinit var dataController: DataController
    private var deviceList = arrayListOf<SmartFarmDevice>()// list of device objects
    private lateinit var addDeviceBtn: FloatingActionButton // fab to add devices
    private lateinit var deviceRecycler: RecyclerView  // recyclerView of the devices


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: Network details fragment")
        val mView = inflater.inflate(R.layout.fragment_network_details, container, false)
        dataController = DataController(mContext)
        initViews(mView)
        fetchNetworksDevices()
        return mView;
    }

    /** This method will fetch all the devices that belong to this network*/
    private fun fetchNetworksDevices() {
        Log.d(TAG, "fetchUsersNetworks: ")
        dataController.fetchNetworkDevices(network, object : DeviceListCallback {
            override fun getDevices(result: Boolean, devices: ArrayList<SmartFarmDevice>?) {
                if (result) {
                    deviceList = devices!! // could be empty
                    updateDevicesList() // refresh the document list
                } else {
                    CodingTools.displayToast(mContext, "Error fetching devices", Toast.LENGTH_SHORT)
                }
            }
        })
    }

    /** This method will refresh the devices list after update / init*/
    private fun updateDevicesList() {
        Log.d(TAG, "updateDevicesList: ")
        val adapter = DeviceListAdapter(
            requireContext(),
            deviceList,
            object : DeviceCallback {
                override fun getDevice(item: SmartFarmDevice) {
                    fetchDeviceDetails(item)
                }

            }
        )
        deviceRecycler.adapter = adapter
    }

    /** This method will fetch the device details from the server and then will open
     * the details fragment*/
    private fun fetchDeviceDetails(item: SmartFarmDevice) {
        Log.d(TAG, "openDeviceWindow: ")
        /** This method will fetch the most recent data from the devices measurement*/
        Log.d(TAG, "fetchLatestData: ")
        dataController.getLastEntry(item.name, object : MeasurementCallback {
            override fun getMeasurement(data: SmartFarmData?) {
                if (data != null) {
                    Log.d(TAG, "getMeasurement: $data")
                    openDetailsFragment(data)
                } else {
                    CodingTools.displayToast(
                        mContext,
                        "Error fetching device details",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        })
    }

    /** This method will open the device details fragment*/
    private fun openDetailsFragment(data: SmartFarmData) {
        Log.d(TAG, "openDetailsFragment: ")
        CodingTools.switchFragment(
            parentFragmentManager,
            R.id.main_LAY_mainFrame,
            DataFragment(mContext, data),
            true,
            "data"
        )
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: NetworkDetails")
        addDeviceBtn = mView.findViewById(R.id.networkDetails_BTN_addNetwork)
        deviceRecycler = mView.findViewById(R.id.networkDetails_LST_networkList)
    }
}