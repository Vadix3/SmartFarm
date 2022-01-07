package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.DB_NAME
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.MyAppClass.Constants.USER_DATA_COLLECTION
import com.example.smartfarm.R
import com.example.smartfarm.controllers.UserController
import com.example.smartfarm.interfaces.LoginListener
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmUser
import com.example.smartfarm.utils.CodingTools
import com.example.smartfarm.utils.MongoTools
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignupFragment(mContext: Context,loginListener:LoginListener) : Fragment() {

    val mContext = mContext;
    val loginListener= loginListener
    private lateinit var nameBox: TextInputEditText
    private lateinit var emailBox: TextInputEditText
    private lateinit var passwordBox: TextInputEditText
    private lateinit var confirmPasswordBox: TextInputEditText
    private lateinit var submitBtn: MaterialButton

    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: HomeFragment")
        val mView = inflater.inflate(R.layout.fragment_signup, container, false)
        initViews(mView)
        return mView;
    }

    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: View) {
        Log.d(MyAppClass.Constants.TAG, "initViews: HomeFragment")

        nameLayout = mView.findViewById(R.id.newAccount_LAY_nameLayout)
        emailLayout = mView.findViewById(R.id.newAccount_LAY_emailLayout)
        passwordLayout = mView.findViewById(R.id.newAccount_LAY_passwordLayout)
        confirmPasswordLayout = mView.findViewById(R.id.newAccount_LAY_confirmPasswordLayout)

        nameBox = mView.findViewById(R.id.newAccount_EDT_name)
        emailBox = mView.findViewById(R.id.newAccount_EDT_email)
        passwordBox = mView.findViewById(R.id.newAccount_EDT_password)
        confirmPasswordBox = mView.findViewById(R.id.newAccount_EDT_confirmPassword)

        submitBtn = mView.findViewById(R.id.newAccount_BTN_submit)
        submitBtn.setOnClickListener {
            val checkResult = checkValidInput() // get a result from the input check
            if (checkResult == "All good!") {
                Log.d(TAG, "check: All good!")
                val temp = SmartFarmUser()
                temp.email = emailBox.text.toString()
                temp.password = passwordBox.text.toString()
                temp.name = nameBox.text.toString()
                saveUserToDB(temp)
            } else {
                Log.d(TAG, "check: $checkResult")
            }
        }
    }

    /** A method that will receive a user object and save the user to the realm*/
    private fun saveUserToDB(temp: SmartFarmUser) {
        Log.d(TAG, "saveUserToDB: $temp")
        UserController(mContext).registerNewUser(temp,object :ResultListener{
            override fun result(result: Boolean, message: String) {
                if(result){
                    Log.d(TAG, "result: $message")
                    UserController(mContext).loginUser(temp.email,temp.password,object:ResultListener{
                        override fun result(result: Boolean, message: String) {
                            if(result){
                                MongoTools.createUserCustomData(DB_NAME, USER_DATA_COLLECTION,object:ResultListener{
                                    override fun result(result: Boolean, message: String) {
                                        if(result){
                                            //TODO: Do this
                                            Log.d(TAG, "result: saved data successfully")
                                        }else{
                                            Log.d(TAG, "result: data wasnt saved")
                                        }
                                    }

                                },"name",temp.name)
                                loginListener.moveToMain(temp.email)
                            }else{
                                CodingTools.displayToast(mContext,"Error: $message",Toast.LENGTH_SHORT)
                            }
                        }
                    })
                }else{
                    Log.d(TAG, "result: ERROR: $message")
                    CodingTools.displayToast(mContext,"ERROR: $message", Toast.LENGTH_SHORT)
                }
            }
        })
    }

    /** A method that will check for valid input in the input boxes*/
    private fun checkValidInput(): String {
        Log.d(TAG, "checkValidInput: ")
        /** A check will be performed on each box and will return false if there is a problem.
         * if there is none, it will return true
         */
        if (nameBox.text.toString().trim().isEmpty()) { // Check first name not empty
            Log.d(TAG, "checkValidInput: No input for first name")
            val problem = "Please enter first name"
            nameLayout.error = problem
            return problem;
        } else {
            nameLayout.error = null
        }

        if (emailBox.text.toString().trim().isEmpty()) { // Check email not empty
            Log.d(TAG, "checkValidInput: No input for email")
            val problem = "Please enter email"
            emailLayout.error = problem
            return problem;
        } else {
            emailLayout.error = null
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailBox.text.toString()).matches()) {
            // Check email format
            Log.d(TAG, "checkValidInput: bad email format")
            val problem = "Please enter proper email"
            emailLayout.error = problem
            return problem;
        } else {
            emailLayout.error = null
        }
        if (passwordBox.text.toString().trim().isEmpty()) { // Check password not empty
            Log.d(TAG, "checkValidInput: No input for password")
            val problem = "Please enter password"
            passwordLayout.error = problem
            return problem;
        } else {
            passwordLayout.error = null
        }
        if (passwordBox.text.toString().length < 6) { // Check password length
            Log.d(TAG, "checkValidInput: Short password") // TODO: password complexity check
            val problem = "Password too short"
            passwordLayout.error = problem
            return problem;
        } else {
            passwordLayout.error = null
        }
        if (confirmPasswordBox.text.toString() != passwordBox.text.toString()) { // Confirm password mismatch
            Log.d(TAG, "checkValidInput: Confirm password mismatch")
            val problem = "Passwords did not match"
            confirmPasswordLayout.error = problem
            return problem;
        } else {
            confirmPasswordLayout.error = null
        }

        return "All good!";
    }
}