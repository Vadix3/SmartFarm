package com.example.smartfarm.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.smartfarm.MyAppClass.Constants.DATA_COLLECTION
import com.example.smartfarm.MyAppClass.Constants.DB_NAME
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.DocumentListListener
import com.example.smartfarm.interfaces.MultipleDataCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmUser
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.iterable.FindIterable
import org.bson.Document
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/** A singleton class that will handle the needed queries to the mongodb server*/

object MongoTools {

    lateinit var app: App
    lateinit var user: User

    /** This method will initialize the connection to Realm*/
    fun initialize(mContext: Context) {
        Realm.init(mContext)
        val appID: String =
            mContext.getString(R.string.app_id)// The app id from Realm
        app = App(AppConfiguration.Builder(appID).build())
    }

    /** This function will check if the given queried document exists in the given db anc collection*/
    fun checkExistingDocument(
        database: String,
        collection: String,
        query: Document,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "checkExistingDocument: MONGOTOOLS")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        mongoCollection.findOne(query).getAsync { result ->
            if (result.isSuccess) {
                if (result.get().getBoolean("active")) { // if the device is already active
                    resultListener.result(false, "Device already active!")
                } else {
                    resultListener.result(true, result.get().getString("did"))
                }
            } else {
                resultListener.result(false, result.error.toString())
            }
        }
    }

    /**
     * This method will fetch the latest entry in the specified collection in the Mongo database
     * and will return it in a JSON form to the caller
     */
    fun fetchLastDocument(
        database: String,
        collection: String,
        query: Document,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "fetchLastDocument: Fetching latest document query:$query")

        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!

        /** This row will fetch the last id from the document using sorting desc and fetching the last result
         * because mongo works like that for fetching the last document
         */
        val cursor: FindIterable<Document> =
            mongoCollection.find(query).sort(Document("_id", -1)).limit(1)

        cursor.iterator().getAsync { result ->
            if (result != null && result.get().hasNext()) {
                val temp = result.get().next()
                resultListener.result(true, temp.toJson())
            } else {
                resultListener.result(false, "No results")
            }
        }
    }

    /** A method to save the user to the server*/
    fun saveUserToServer(
        context: Context,
        tempUser: SmartFarmUser,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "saveUserToServer: tools")
        app.emailPassword.registerUserAsync(tempUser.email, tempUser.password) {
            if (!it.isSuccess) {
                Log.e(TAG, "Error in saveUserToServer: ${it.error}")
                resultListener.result(false, "" + it.error)
            } else {
                Log.i(TAG, "Successfully registered user.")
                Toast.makeText(context, "User created successfully!", Toast.LENGTH_SHORT).show()
                resultListener.result(true, "Success: $it")
            }
        }
    }

    /** This method will update the given queried document with the given document*/
    fun updateDocument(
        database: String,
        collection: String,
        document: Document,
        query: Document,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "putDocument: MongoTools $document query: $query")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        mongoCollection.updateOne(query, document).getAsync { result ->
            if (result.isSuccess) {
                resultListener.result(true, result.get().toString())
            } else {
                resultListener.result(false, result.error.toString())
            }
        }
    }

    /** This method will put the given document in the given collection and db*/
    fun putDocument(
        database: String,
        collection: String,
        document: Document,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "putDocument: MongoTools")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        mongoCollection.insertOne(document).getAsync { result ->
            if (result.isSuccess) {
                val id = result.get().insertedId
                Log.d(TAG, "putDocument: successful insert: $id")
                resultListener.result(true, id.toString())
            } else {
                resultListener.result(false, result.error.toString())
            }
        }
    }

    fun loginUser(
        context: Context,
        email: String,
        password: String,
        resultListener: ResultListener
    ) {
        Log.d(TAG, "loginUser: tools")
        val cred: Credentials = Credentials.emailPassword(email, password)
        app?.loginAsync(cred) {
            if (it.isSuccess) {
                user = app.currentUser()!!
                if (user != null) {
                    resultListener.result(true, "Logged in: ${user.id}")
                } else {
                    resultListener.result(false, "User is null")
                }
            } else {
                val error = it.error.errorCode.ordinal

                if (error == 174) {
                    Log.d(TAG, "loginApp: Email/Password problem")
                    resultListener.result(false, "Email/password problem")
                } else {
                    resultListener.result(false, error.toString())
                }
            }
        }
    }

    /**
     * This method will fetch the latest entry in the specified collection in the Mongo database
     * and will return it in a JSON form to the caller
     */
    fun fetchDocuments(
        database: String,
        collection: String,
        query: Document,
        resultListener: DocumentListListener
    ) {
        Log.d(TAG, "fetchLastDocument: Fetching documents for: $query")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!

        /** This row will fetch the last id from the document using sorting desc and fetching the last result
         * because mongo works like that for fetching the last document
         */
        val cursor: FindIterable<Document> =
            mongoCollection.find(query)
        val resultList = arrayListOf<Document>()
        cursor.iterator().getAsync { result ->
            if (result != null) { // connection established
                if (result.get() != null) {  // got results, return json of list
                    while (result.get().hasNext()) {
                        val temp = result.get().next()
                        Log.d(TAG, "fetchDocuments: temp = $temp")
                        Log.d(TAG, "onResult: $temp")
                        resultList.add(temp)
                    }
                    resultListener.getDocuments(true, resultList)
                } else { // connection problem, return null string
                    resultListener.getDocuments(false, null)
                }
            }
        }
    }

    /** This method will listen for changes in the Data document, and will return the new data
     * using the provided callback
     */
    fun listenForChanges(
        dbName: String,
        collection: String,
        listener: ResultListener,
        filter: Document
    ) {
        Log.d(TAG, "listenForChanges TOOLS:db: $dbName collection: $collection, filter: $filter")

        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(dbName)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        val watcher = mongoCollection
            .watchWithFilterAsync(filter)
        watcher[{ result ->
            if (result.isSuccess) {
                val full = result.get().fullDocument!!.toJson().toString()
                listener.result(true, full)
            } else {
                listener.result(false, result.error.toString())
            }
        }]
    }

    fun getDocumentsByDayAndHours(
        deviceId: String,
        startDay: LocalDate,
        endDay: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        listener: MultipleDataCallback
    ) {
        Log.d(
            TAG,
            "getDocumentsByDayAndHours: startDay: $startDay endDay: $endDay startTime: $startTime endTime:$endTime"
        )
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(DB_NAME)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(DATA_COLLECTION)!!
        val cursor: FindIterable<Document> =
            mongoCollection.find()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)

        val resultList = arrayListOf<SmartFarmData>()
        cursor.iterator().getAsync { result ->
            if (result != null) { // connection established
                if (result.get() != null) {  // got results, return json of list
                    while (result.get().hasNext()) {
                        val item = result.get().next()
                        val itemDate = item["date"] as String
                        val itemTime = item["time"] as String
                        val itemLocalDate = LocalDate.parse(itemDate, dateFormatter)
                        val itemLocalTime = LocalTime.parse(itemTime, timeFormatter)
                        if (item["device"] == deviceId) { // same device

                            if (itemLocalDate.isEqual(startDay) || itemLocalDate.isEqual(endDay)) { // days match
                                Log.d(TAG, "Got item: $item")

                                // item from day before
                                if (itemLocalDate.isEqual(startDay)) {
                                    // if the item time is after the start and before the end of the day
                                    if (itemLocalTime.isAfter(startTime) && itemLocalTime.isBefore(
                                            LocalTime.parse("23:59:59.63")
                                        )
                                    ) {
                                        //time match
                                        resultList.add(ParsingTools.parseMeasurement(JSONObject(item.toJson())))
                                    }
                                }

                                //Item from later day
                                if (itemLocalDate.isEqual(endDay)) {
                                    // item after start of later day and before end time
                                    if (itemLocalTime.isAfter(LocalTime.parse("00:00:00.63")) && itemLocalTime.isBefore(
                                            endTime
                                        )
                                    ) {
                                        //time match
                                        resultList.add(ParsingTools.parseMeasurement(JSONObject(item.toJson())))
                                    }
                                }
                            }
                        }
                    }
                    listener.getData(resultList)
                } else {
                    Log.d(TAG, "getDocumentsByDayInterval: Null result")
                }
            }
        }
    }


    fun getDocumentsByDayInterval(
        deviceId: String,
        start: LocalDate,
        end: LocalDate,
        listener: MultipleDataCallback
    ) {
        Log.d(TAG, "getDocumentsByDayInterval: start: $start end: $end")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(DB_NAME)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(DATA_COLLECTION)!!
        val cursor: FindIterable<Document> =
            mongoCollection.find()

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH)
        val resultList = arrayListOf<SmartFarmData>()
        cursor.iterator().getAsync { result ->
            if (result != null) { // connection established
                if (result.get() != null) {  // got results, return json of list
                    while (result.get().hasNext()) {
                        val item = result.get().next()
                        val itemDate = item["date"] as String
                        val itemLocalDate = LocalDate.parse(itemDate, formatter)
                        if (item["device"] == deviceId) {
                            if (itemLocalDate.isAfter(start) && itemLocalDate.isBefore(end)) {
                                resultList.add(ParsingTools.parseMeasurement(JSONObject(item.toJson())))
                            }
                        }
                    }
                    listener.getData(resultList)
                } else {
                    Log.d(TAG, "getDocumentsByDayInterval: Null result")
                }
            }
        }
    }
}

