package com.example.smartfarm.interfaces

import com.example.smartfarm.models.SmartFarmDevice

/** This interface will send a list of SmartFarm devices*/
interface DeviceListCallback {
    fun getDevices(result: Boolean, devices: ArrayList<SmartFarmDevice>?)
}