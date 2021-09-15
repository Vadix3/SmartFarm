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

    }


    companion object Functions {
        fun displayToast(myContext: Context, message: String) {
            Toast.makeText(myContext, message, Toast.LENGTH_LONG).show()
        }
    }

}