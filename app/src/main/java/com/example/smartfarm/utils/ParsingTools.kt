package com.example.smartfarm.utils

import android.util.Log
import com.example.smartfarm.MyAppClass.Constants.DRY_WATER
import com.example.smartfarm.MyAppClass.Constants.MODERATE_MINUS_WATER
import com.example.smartfarm.MyAppClass.Constants.MODERATE_PLUS_WATER
import com.example.smartfarm.MyAppClass.Constants.MODERATE_WATER
import com.example.smartfarm.MyAppClass.Constants.SOAKED
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.models.*
import org.bson.Document
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

object ParsingTools {
    fun parseMeasurement(plainJson: JSONObject): SmartFarmData {
        Log.d(TAG, "parseMeasurement: $plainJson")
        val data = SmartFarmData()
        data.date = plainJson.get("date") as String
        data.time = plainJson.get("time") as String
        try {
            data.humidity = plainJson.get("humidity") as Double
        } catch (e: Exception) {
            data.humidity = -999.0
        }
        try {
            data.temperature = plainJson.get("temperature") as Double
        } catch (e: Exception) {
            data.temperature = -999.0
        }
        try {
            data.soil = plainJson.get("soil") as Int
        } catch (e: Exception) {
            data.soil = -999
        }
        try {
            data.light = plainJson.get("light") as Int
        } catch (e: Exception) {
            data.light = -999
        }
        try {
            data.uv = plainJson.get("uv") as Int
        } catch (e: Exception) {
            data.uv = -999
        }
        try {
            data.uv = plainJson.get("uv") as Int
        } catch (e: Exception) {
            data.uv = -999
        }
        data.device = plainJson.get("device") as String
        return data
    }

    fun networkToDocument(network: SmartFarmNetwork): Document {
        Log.d(TAG, "networkToDocument: ")
        val document = Document()
        document.append("name", network.name)
        document.append("owner", network.owner)
        document.append("devices", network.devices)
        return document
    }

    fun parseNetwork(item: Document): SmartFarmNetwork {
        Log.d(TAG, "parseNetwork: Parsing: $item")
        val data = SmartFarmNetwork()
        data.id = item["_id"].toString()
        data.name = item["name"] as String
        data.owner = item["owner"] as String
        data.devices = item["devices"] as ArrayList<String>
        return data
    }

    fun parseDevice(item: Document): SmartFarmDevice {
        Log.d(TAG, "parseDevice: Parsing: $item")
        val data = SmartFarmDevice()
        data.did = item["did"].toString()
        data.produce = item["produce"] as String
        data.name = item["name"] as String
        data.active = item["active"] as Boolean
        data.description = item["description"] as String
        data.measure_interval = item["measure_interval"] as Double
        return data
    }

    fun deviceToDocument(device: SmartFarmDevice): Document {
        val document = Document()
        document.append("name", device.name)
        document.append("description", device.description)
        document.append("did", device.did)
        document.append("active", device.active)
        document.append("produce", device.produce)
        document.append("measure_interval", device.measure_interval)
        return document
    }


    fun parseWeather(item: JSONObject): WeatherReport {
        val data = WeatherReport()
        val current = item.get("current") as JSONObject
        val inner = ((current.get("weather") as JSONArray)[0]) as JSONObject
        data.date = current.getLong("dt")
        data.feels_like = current.getDouble("feels_like")
        data.humidity = current.getInt("humidity")
        data.sunrise = current.getLong("sunrise")
        data.sunset = current.getLong("sunset")
        data.temp = current.getDouble("temp")
        data.wind_speed = current.getDouble("wind_speed")
        data.icon = inner.getString("icon")
        data.weather_description = inner.getString("description")
        return data
    }

    fun parseDailyWeather(item: JSONObject): ArrayList<WeatherReport> {
        val list = arrayListOf<WeatherReport>()
        val plainList = item.get("daily") as JSONArray
        for (i in 0 until plainList.length()) {
            val current = plainList.get(i) as JSONObject
            val data = WeatherReport()
            val inner = ((current.get("weather") as JSONArray)[0]) as JSONObject
            data.date = current.getLong("dt")
            data.feels_like = (current.get("feels_like") as JSONObject).getDouble("day")
            data.humidity = current.getInt("humidity")
            data.sunrise = current.getLong("sunrise")
            data.sunset = current.getLong("sunset")
            data.temp = (current.get("temp") as JSONObject).getDouble("day")
            data.wind_speed = current.getDouble("wind_speed")
            data.icon = inner.getString("icon")
            data.weather_description = inner.getString("description")
            list.add(data)
        }
        return list
    }

    fun commandToDocument(command: CommandModel): Document {
        val document = org.bson.Document()
        document.append("id", command.id)
        document.append("device_id", command.deviceId)
        document.append("time", command.time)
        document.append("type", command.type)
        document.append("data", command.data)
        return document
    }
}