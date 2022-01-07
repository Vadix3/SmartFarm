package com.example.smartfarm.controllers

import android.content.Context
import android.util.Log
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.DATA_COLLECTION
import com.example.smartfarm.MyAppClass.Constants.DB_NAME
import com.example.smartfarm.MyAppClass.Constants.DEVICES_COLLECTION
import com.example.smartfarm.MyAppClass.Constants.NETWORKS_COLLECTION
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.interfaces.*
import com.example.smartfarm.models.CommandModel
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import com.example.smartfarm.utils.MongoTools
import com.example.smartfarm.utils.ParsingTools
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import kotlin.collections.ArrayList

/** The class will perform the needed actions to get the data from the server*/
class DataController(context: Context) {

    private val context = context
    private var tools = MongoTools

    /**
     * This method will fetch the latest entry for the given device, and return it to the user.
     */
    fun getLastEntry(device: String, listener: MeasurementCallback) {
        val query = Document("device", device)
        tools.fetchLastDocument(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.DATA_COLLECTION,
            query,
            object : ResultListener {
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


    fun checkExistingDocument(
        database: String,
        collection: String,
        did: String,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "checkExistingDocument: Controller")
        tools.checkExistingDocument(database, collection, Document("did", did), resultListener)
    }

    /** This function will send a document to MongoTools that will insert it to the cloud*/
    fun initDevice(
        device: SmartFarmDevice,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "initDevice: Controller")
        tools.updateDocument(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.DEVICES_COLLECTION,
            ParsingTools.deviceToDocument(device),
            Document("did", device.did),
            resultListener
        )
    }

    /** This function will add the given did to the selected network*/
    fun addDeviceToNetwork(
        did: String,
        network: SmartFarmNetwork,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "initDevice: Controller")
        network.devices.add(did)
        tools.updateDocument(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.NETWORKS_COLLECTION,
            ParsingTools.networkToDocument(network),
            Document("_id", ObjectId(network.id)),
            resultListener
        )
    }


    /** This function will send a document to MongoTools that will insert it to the cloud*/
    fun initNetwork(
        database: String,
        collection: String,
        network: SmartFarmNetwork,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "insertDocument: Controller")
        tools.putDocument(
            database,
            collection,
            ParsingTools.networkToDocument(network),
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        resultListener.result(true, message)
                    } else {
                        resultListener.result(false, message)
                    }
                }
            })
    }

    /** This method will send a command object to the cloud.
     * the command will consist of:
     * - The type of the command
     * - Any extra data for the command
     * Current command types:
     * TURN_OFF_COMMAND = 2
     * TURN_ON_COMMAND = 1
     * CHANGE_DURATION_COMMAND = 0
     */
    fun sendCommand(command: CommandModel, listener: ResultListener) {
        Log.d(TAG, "sendTestCommand: ")
        val document = ParsingTools.commandToDocument(command)
        MongoTools.putDocument(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.COMMANDS_COLLECTION,
            document,
            listener
        )
    }

    /** This method will return a list of the fetched networks
     * the method will send a query with the users email and will
     * fetch all the networks that their owner is the user*/
    fun fetchUsersNetworks(listener: NetworkListCallback) {
        val userEmail = MyAppClass.Constants.USER_EMAIL
        Log.d(TAG, "fetchUsersNetworks: fetching networks for: $userEmail")
        val query = Document("owner", userEmail)
        tools.fetchDocuments(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.NETWORKS_COLLECTION,
            query,
            object : DocumentListListener {
                override fun getDocuments(result: Boolean, list: ArrayList<Document>?) {
                    if (result) { // got documents
                        val networkList = arrayListOf<SmartFarmNetwork>()
                        for (item in list!!) {
                            networkList.add(ParsingTools.parseNetwork(item))
                        }
                        // Return the network list
                        listener.getNetworks(true, networkList)
                    } else { // got nothing
                        listener.getNetworks(false, null)
                    }
                }
            })
    }

    /** This method will fetch all the devices for the network and return them
     * as a list of objects. An empty list would mean that there are no devices in
     * the network. A null list would mean an error has occurred
     */
    fun fetchNetworkDevices(network: SmartFarmNetwork, deviceListCallback: DeviceListCallback) {
        Log.d(TAG, "fetchNetworkDevices: ")
        val list = network.devices // The list of the device ID's to fetch from the server
        val query = Document("did", Document("\$in", list))
        tools.fetchDocuments(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.DEVICES_COLLECTION,
            query,
            object : DocumentListListener {
                override fun getDocuments(result: Boolean, list: ArrayList<Document>?) {
                    if (result) { // got documents
                        val devicesList = arrayListOf<SmartFarmDevice>()
                        for (item in list!!) {
                            val device = ParsingTools.parseDevice(item)
                            if (device.active) { // add only active devices
                                devicesList.add(ParsingTools.parseDevice(item))
                            }
                        }
                        // Return the network list
                        deviceListCallback.getDevices(true, devicesList)
                    } else { // got nothing
                        deviceListCallback.getDevices(false, null)
                    }
                }
            })
    }

    /** This method will update the device document in the cloud*/
    fun updateDevice(device: SmartFarmDevice, listener: ResultListener) {
        Log.d(TAG, "updateDevice: Controller:")
        MongoTools.updateDocument(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.DEVICES_COLLECTION,
            ParsingTools.deviceToDocument(device),
            Document("did", device.did),
            listener
        )
    }

    /** This method will listen for data measurement changes in the data document
     * it will return the new object / null in case of no new objects
     */
    fun listenForDataChanges(measurementCallback: MeasurementCallback, did: String) {
        Log.d(TAG, "listenForDataChanges: Controller")
        MongoTools.listenForChanges(DB_NAME, DATA_COLLECTION, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) {
                    Log.d(TAG, "result: New data = $message")
                    val fixedJson = JSONObject(message)
                    measurementCallback.getMeasurement(
                        ParsingTools.parseMeasurement(
                            fixedJson
                        )
                    )
                } else {
                    measurementCallback.getMeasurement(null)
                }
            }
        }, Document("fullDocument.device", did))
    }

    /** This item will fetch all the devices of the user
     * TODO: Maybe a username will be needed at some point
     */
    fun fetchAllDevicesOfUser(listener: DeviceListCallback) {
        Log.d(TAG, "fetAllDevicesOfUser: Controller")
        MongoTools.fetchAllDocuments(
            DB_NAME,
            DEVICES_COLLECTION,
            object : DocumentListListener {
                override fun getDocuments(result: Boolean, list: ArrayList<Document>?) {
                    if (result) {
                        val deviceList = arrayListOf<SmartFarmDevice>()
                        for (item in list!!) {
                            deviceList.add(ParsingTools.parseDevice(item))
                        }
                        listener.getDevices(true, deviceList)
                    } else {
                        listener.getDevices(false, arrayListOf<SmartFarmDevice>())
                    }
                }

            })
    }

    /**
     * This method will get 2 dates, 2 times and a listener, and will return to the listener a
     * list of data objects for the given date and time
     */
    fun getDataByDateAndTime(
        deviceId: String,
        start: LocalDate,
        end: LocalDate,
        sTime: LocalTime,
        eTime: LocalTime,
        listener: MultipleDataCallback
    ) {
        Log.d(TAG, "getDataByRange: Controller: start:$start end:$end")
        MongoTools.getDocumentsByDayAndHours(deviceId, start, end, sTime, eTime, listener)
    }


    /**
     * This method will get 2 dates and a listener, and will return to the listener a
     * list of data objects for the given date
     */
    fun getDataByDate(
        deviceId: String,
        start: LocalDate,
        end: LocalDate,
        listener: MultipleDataCallback
    ) {
        Log.d(TAG, "getDataByRange: Controller: start:$start end:$end")
        MongoTools.getDocumentsByDayInterval(
            deviceId,
            start.minusDays(1),
            end.plusDays(1),
            listener
        )
    }

    /** This method will fetch all the networks of the user*/
    fun fetchAllNetworksOfUser(listener: NetworkListCallback) {
        Log.d(TAG, "fetchAllNetworksOfUser: ")
        MongoTools.fetchAllDocuments(
            DB_NAME,
            NETWORKS_COLLECTION,
            object : DocumentListListener {
                override fun getDocuments(result: Boolean, list: ArrayList<Document>?) {
                    if (result) {
                        val networkList = arrayListOf<SmartFarmNetwork>()
                        for (item in list!!) {
                            networkList.add(ParsingTools.parseNetwork(item))
                        }
                        listener.getNetworks(true, networkList)
                    } else {
                        listener.getNetworks(false, arrayListOf<SmartFarmNetwork>())
                    }
                }

            })
    }
}