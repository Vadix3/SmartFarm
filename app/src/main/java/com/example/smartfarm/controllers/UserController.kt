package com.example.smartfarm.controllers

import android.content.Context
import android.util.Log
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmUser
import com.example.smartfarm.utils.MongoTools

/** The class will perform the needed actions to get the data from the server*/
class UserController(context: Context) {

    private val context = context
    private var tools = MongoTools

    init{
        tools.initialize(context)
    }
    fun registerNewUser(user:SmartFarmUser,listener: ResultListener){
        Log.d(TAG, "registerNewUser: controller ")
        MongoTools.saveUserToServer(context,user,object:ResultListener{
            override fun result(result: Boolean, message: String) {
                if(result){
                    listener.result(true,message)
                }else{
                    listener.result(false,message)
                }
            }
        })
    }

    /** A method that will send the email and password to the server and will try to perform
     * a login using the mongotools.
     */
    fun loginUser(email:String,password:String,listener: ResultListener){
        Log.d(TAG, "loginUser: controller")
        MongoTools.loginUser(context,email,password,object:ResultListener{
            override fun result(result: Boolean, message: String) {
                if(result){
                    listener.result(true,message)
                }else{
                    listener.result(false,message)
                }
            }

        })
    }

}