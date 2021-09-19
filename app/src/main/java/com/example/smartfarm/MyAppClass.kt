package com.example.smartfarm

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration


class MyAppClass: Application() {

    object Constants {
        const val TAG = "myTag"
        const val dbName = "testdb"
        const val dataCollection = "data"
        const val devicesCollection = "devices"
        const val networksCollection = "networks"
        var userEmail = ""
    }

}