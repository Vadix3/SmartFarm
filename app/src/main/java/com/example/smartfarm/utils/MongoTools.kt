package com.example.smartfarm.utils

import android.content.Context
import android.util.Log
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
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

/** A singleton class that will handle the needed queries to the mongodb server*/

object MongoTools {

    private lateinit var app: App
    private lateinit var user: User


    /** The method will initialize the connection to mongodb realm
     *  and will return the result of the connection attempt*/
    fun initMongoTools(context: Context, resultListener: ResultListener) {
        Log.d(TAG, "initTools: Initializing mongo tools")
        Realm.init(context)
        val appID: String =
            context.getString(R.string.app_id)// The app id from Realm
        app = App(AppConfiguration.Builder(appID).build())
        val credentials: Credentials = Credentials.anonymous()
        app.loginAsync(credentials) {
            if (it.isSuccess) {
                user = app.currentUser()!!
                resultListener.result(true, "Connection established successfully")
            } else {
                resultListener.result(false, "Connection failed: ${it.error}")
            }
        }
    }

    /**
     * This method will fetch the latest entry in the specified collection in the Mongo database
     * and will return it in a JSON form to the caller
     */
    fun fetchLastDocument(database: String, collection: String, resultListener: ResultListener) {
        Log.d(TAG, "fetchLastDocument: Fatching latest document")

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
            mongoCollection.find().sort(Document("_id", -1)).limit(1)

        cursor.iterator().getAsync { result ->
            if (result != null) {
                val temp = result.get().next()
                resultListener.result(true, temp.toJson())
            } else {
                resultListener.result(false, "No results")
            }
        }
    }

}
