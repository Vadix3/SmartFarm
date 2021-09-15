package com.example.smartfarm.controllers

import android.content.Context
import android.util.Log
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.interfaces.MeasurementCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.utils.MongoTools
import com.example.smartfarm.utils.ParsingTools
import org.json.JSONObject

/** The class will perform the needed actions to get the data from the server*/
class MeasuredDataController(context: Context, resultListener: ResultListener) {

    private val context = context
    private val resultListener = resultListener
    private var tools = MongoTools
    private val database = "testdb"
    private val collection = "data"

    /** Initialize the mongodb singleton class and establish initial connection to it.
     * Once the connection is established we can perform queries.
     * The result of the connection attempt will be sent back to the caller.
     */
    init {
        tools.initMongoTools(context, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) {
                    resultListener.result(true, message)
                } else {
                    resultListener.result(false, message)
                }
            }
        })
    }

    /**
     * This method will fetch the latest entry in the specified collection and will return it
     * in JSON form to the caller
     */
    fun getLastEntry(listener: MeasurementCallback) {
        tools.fetchLastDocument(database, collection, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) {
                    Log.d(TAG, "result: SUCCESS: $message")

                    // This line will return the fetched measurement as an object
                    listener.getMeasurement(ParsingTools.parseMeasurement(JSONObject(message)))
                } else {
                    Log.d(TAG, "result: ERROR: $message")
                    listener.getMeasurement(null)
                }
            }
        })
    }
}