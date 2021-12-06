package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.activities.MainActivity
import com.example.smartfarm.controllers.UserController
import com.example.smartfarm.interfaces.LoginListener
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment(mContext: Context, loginListener: LoginListener) : Fragment() {

    private val mContext = mContext
    private val loginListener = loginListener
    private lateinit var signupButton: TextView
    private lateinit var loginButton: MaterialButton
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailEdt: TextInputEditText
    private lateinit var passwordEdt: TextInputEditText


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: fragmentLogin ")
        val mView = inflater.inflate(R.layout.fragment_login, container, false);
        initViews(mView)

        /**For testing, remove later*/
        loginApp("vadix3@gmail.com", "121212")
        return mView
    }

    private fun initViews(mView: View) {
        signupButton = mView.findViewById(R.id.login_BTN_signup)
        loginButton = mView.findViewById(R.id.login_BTN_login)

        /** The signup button will move the user to the signup fragment, and will add the
         * transaction to the backstack
         */
        signupButton.setOnClickListener {
            CodingTools.switchFragment(
                parentFragmentManager,
                R.id.main_LAY_mainFrame,
                SignupFragment(mContext, loginListener),
                true,
                "login_signup"
            )
        }
        loginButton.setOnClickListener {
            checkUserInput()
        }

        emailLayout = mView.findViewById(R.id.login_LAY_emailLayout)
        passwordLayout = mView.findViewById(R.id.login_LAY_passwordLayout)

        emailEdt = mView.findViewById(R.id.login_Edt_emailEdt)
        emailEdt.addTextChangedListener { emailLayout.error = null }
        passwordEdt = mView.findViewById(R.id.login_Edt_passwordEdt)
        passwordEdt.addTextChangedListener { passwordLayout.error = null }

    }

    private fun checkUserInput() {
        Log.d(TAG, "checkUserInput: ")
        var flag = false
        if (emailEdt.text.toString().trim().isEmpty()) { // Check email not empty
            Log.d(TAG, "checkValidInput: No input for email")
            val problem = "Please enter email"
            emailLayout.error = problem
            flag = true
        } else {
            emailLayout.error = null
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEdt.text.toString()).matches()) {
            // Check email format
            Log.d(TAG, "checkValidInput: bad email format")
            val problem = "Please enter a proper email address"
            emailLayout.error = problem
            flag = true
        } else {
            emailLayout.error = null
        }
        if (passwordEdt.text.toString().trim().isEmpty()) { // Check password not empty
            Log.d(TAG, "checkValidInput: No input for password")
            val problem = "Please enter password"
            passwordLayout.error = problem
            flag = true
        } else {
            passwordLayout.error = null
        }
        if (!flag) {
            Log.d(TAG, "checkUserInput: No problems!")
            loginApp(emailEdt.text.toString().lowercase(), passwordEdt.text.toString())
        }
    }

    private fun loginApp(email: String, password: String) {
        Log.d(TAG, "loginApp with: $email")
        UserController(mContext).loginUser(email, password, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) {
                    loginListener.moveToMain(email)
                } else {
                    Log.d(TAG, "result: message = $message")
                    if (message == "1") {
                        CodingTools.displayErrorDialog(
                            mContext,
                            mContext.getString(R.string.no_internet_connection)
                        )
                    }
                    CodingTools.displayToast(mContext, "Error: $message", Toast.LENGTH_SHORT)
                }
            }
        })

    }
}