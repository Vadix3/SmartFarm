package com.example.smartfarm.interfaces

import com.example.smartfarm.models.SmartFarmDevice

interface DeviceCallback {
    fun getDevice(item:SmartFarmDevice)
}