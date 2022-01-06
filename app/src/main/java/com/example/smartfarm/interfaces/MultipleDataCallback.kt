package com.example.smartfarm.interfaces

import com.example.smartfarm.models.SmartFarmData

interface MultipleDataCallback {
    fun getData(data: ArrayList<SmartFarmData>?)

}