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

        const val WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT
        var USER_EMAIL = ""
    }

}