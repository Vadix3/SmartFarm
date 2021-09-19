package com.example.smartfarm.interfaces

import com.example.smartfarm.models.SmartFarmNetwork

interface NetworkListCallback {
    fun getNetworks(result:Boolean,networks: ArrayList<SmartFarmNetwork>?)
}