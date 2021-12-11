package com.example.smartfarm

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.view.WindowManager
import android.widget.Toast
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration


class MyAppClass : Application() {

    object Constants {
        const val TAG = "myTag"
        const val DB_NAME = "testdb"
        const val DATA_COLLECTION = "data"
        const val DEVICES_COLLECTION = "devices"
        const val NETWORKS_COLLECTION = "networks"
        const val COMMANDS_COLLECTION = "commands"
        const val BT_MODE = "qr"
        const val QR_MODE = "bt"
        const val LOCATION_PERMISSION = 0
        const val CAMERA_PERMISSION = 1
        const val NETWORK_LIST = "networkList"
        const val DEVICE_LIST = "deviceList"
        const val WEATHER_DATA = "weatherData"
        const val LIST_OF_DEVICES = "devices"
        const val CURRENT_WEATHER = 0
        const val DAILY_WEATHER = 1
        const val TURN_OFF_COMMAND = 2
        const val TURN_ON_COMMAND = 1
        const val CHANGE_DURATION_COMMAND = 0


        const val INTERVAL_COMMAND = 0
        const val SWITCH_COMMAND = 1
        const val MEASURE_NOW_COMMAND = 2
        const val WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT
        var USER_EMAIL = ""

        // measure data constants
        const val HUMIDITY = 4
        const val TEMPERATURE = 1
        const val SOIL_MOISTURE = 0
        const val LIGHT_EXPOSURE = 2
        const val UV_EXPOSURE = 3


        /**
         * - Water possible values (current measurement, very approximate):
         *      moderate =  550-600
         *      moderate+ = 600-650
         *      moderate- = 500-600
         *
         * - Sun possible values:
         *      full or partial = 20-100
         *      partial or shade = 10-50
         *      full = 50-100
         *      shade or no sun = 0-10
         *      */

        const val MODERATE_WATER = "Moderate"
        const val MODERATE_PLUS_WATER = "Moderate+"
        const val MODERATE_MINUS_WATER = "Moderate-"
        const val DRY_WATER = "Dry"
        const val SOAKED = "Soaked"
        const val FULL_OR_PARTIAL_SUN = "Full or partial"
        const val PARTIAL_OR_SHADE_SUN = "Partial or shade"
        const val FULL_SUN = "Full"
        const val SHADE_OR_NO_SUN = "Shade or no sun"

        const val MODERATE_WATER_FLAG = 2
        const val MODERATE_PLUS_WATER_FLAG = 3
        const val MODERATE_MINUS_WATER_FLAG = 1
        const val DRY_WATER_FLAG = 0
        const val SOAKED_FLAG = 4
        const val FULL_OR_PARTIAL_SUN_FLAG = 2
        const val PARTIAL_OR_SHADE_SUN_FLAG = 1
        const val FULL_SUN_FLAG = 3
        const val SHADE_OR_NO_SUN_FLAG = 0

    }

}