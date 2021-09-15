package com.example.smartfarm.utils

import android.util.Log
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.models.MeasuredData
import org.json.JSONObject

object ParsingTools {
    fun parseMeasurement(plainJson: JSONObject):MeasuredData {
        Log.d(TAG, "parseMeasurement: $plainJson")
        val data = MeasuredData()
        data.date=plainJson.get("date") as String
        data.time=plainJson.get("time") as String
        data.humidity=plainJson.get("humidity") as Double
        data.temperature=plainJson.get("temperature") as Double
        data.soil=plainJson.get("soil") as Int
        data.light=plainJson.get("light") as Int
        data.uv =plainJson.get("uv") as Int
        data.device=plainJson.get("device") as String
        return data
    }
}