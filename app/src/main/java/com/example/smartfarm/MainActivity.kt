package com.example.smartfarm


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.smartfarm.controllers.MeasuredDataController
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.interfaces.MeasurementCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.MeasuredData


class MainActivity : AppCompatActivity() {

    private lateinit var dataController:MeasuredDataController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testDbConnection()
    }

    private fun testDbConnection() {
        Log.d(TAG, "testDb: ")
        dataController = MeasuredDataController(this,object: ResultListener {
            override fun result(result: Boolean, message: String) {
                if(result){
                    Log.d(TAG, "Main result: SUCCESS: $message")
                    readLatestData()
                }else{
                    Log.d(TAG, "Main result: ERROR: $message")
                }
            }
        })
    }

    private fun readLatestData() {
        Log.d(TAG, "readLatestData: ")
        dataController.getLastEntry(object:MeasurementCallback{
            override fun getMeasurement(data: MeasuredData?) {
                if(data!=null){
                    Log.d(TAG, "getMeasurement: latest data: $data")
                }else{
                    Log.d(TAG, "getMeasurement: latest data is null!")
                }
            }
        })
    }
}