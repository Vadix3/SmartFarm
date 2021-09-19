package com.example.smartfarm.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.DocumentListListener
import com.example.smartfarm.interfaces.ResultListener
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
            if (result != null) {
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
                Log.e(TAG, "Failed to log in anonymously: $error")
                if (error == 174) {
                    Log.d(TAG, "loginApp: Email/Password problem")
                    resultListener.result(false, "Email/password problem")
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

}

