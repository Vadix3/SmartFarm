package com.example.smartfarm.interfaces

import com.example.smartfarm.models.MeasuredData

/** An interface that will return a measurement object*/
interface MeasurementCallback {
    fun getMeasurement(data: MeasuredData?)

}