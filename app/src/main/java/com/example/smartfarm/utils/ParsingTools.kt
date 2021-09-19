package com.example.smartfarm.utils

import android.util.Log
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import org.bson.Document
import org.json.JSONObject

object ParsingTools {
    fun parseMeasurement(plainJson: JSONObject):SmartFarmData {
        Log.d(TAG, "parseMeasurement: $plainJson")
        val data = SmartFarmData()
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

    fun networkToDocument(network: SmartFarmNetwork):Document{
        Log.d(TAG, "networkToDocument: ")
        val document = Document()
        document.append("name",network.name)
        document.append("owner",network.owner)
        document.append("devices",network.devices)
        return document
    }

    fun parseNetwork(item: Document): SmartFarmNetwork {
        Log.d(TAG, "parseNetwork: Parsing: $item")
        val data = SmartFarmNetwork()
        data.id= item["_id"].toString()
        data.name= item["name"] as String
        data.owner= item["owner"] as String
        data.devices= item["devices"] as ArrayList<String>
        return data
    }

    fun parseDevice(item: Document): SmartFarmDevice {
        Log.d(TAG, "parseDevice: Parsing: $item")
        val data = SmartFarmDevice()
        data.did=item["id"].toString()
        data.name=item["name"] as String
        data.description=item["description"] as String
        return data
    }
}