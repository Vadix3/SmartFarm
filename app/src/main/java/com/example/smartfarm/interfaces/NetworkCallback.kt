package com.example.smartfarm.interfaces

import com.example.smartfarm.models.SmartFarmNetwork

interface NetworkCallback {
    fun getNetwork(network:SmartFarmNetwork)
}