package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
import com.google.android.material.button.MaterialButton

class FirstNetworkDialog(context: Context, resultListener: ResultListener) :
    Dialog(context) {

    private val resultListener = resultListener
    private lateinit var yesBtn: MaterialButton
    private lateinit var noBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_first_network)
        initViews()
    }

    /** Init the views*/
    private fun initViews() {
        Log.d(TAG, "initViews: FirstNetworkDialog ")
        yesBtn = findViewById(R.id.initNetwork_BTN_yes)
        yesBtn.setOnClickListener {
            resultListener.result(true, "")
            dismiss()
        }
        noBtn = findViewById(R.id.initNetwork_BTN_no)
        noBtn.setOnClickListener {
            dismiss()
        }
    }
}