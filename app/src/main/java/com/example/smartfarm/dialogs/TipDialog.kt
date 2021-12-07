package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.models.ProduceTip
import com.google.android.material.textview.MaterialTextView
import kotlin.math.roundToInt

class TipDialog(context: Context, tip: ProduceTip) :
    Dialog(context) {

    private val mContext = context
    private val tips = tip

    private lateinit var name: MaterialTextView
    private lateinit var measuredString: MaterialTextView
    private lateinit var measuredValue: MaterialTextView
    private lateinit var optimalString: MaterialTextView
    private lateinit var optimalValue: MaterialTextView
    private lateinit var recommandation: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: $tips")
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_produce_tip)
        initViews()
    }

    /** Init the views*/
    private fun initViews() {
        Log.d(MyAppClass.Constants.TAG, "initViews: FirstNetworkDialog ")
        name = findViewById(R.id.produceTip_LAY_measureName)
        name.text = tips.name
        measuredString = findViewById(R.id.tipDialog_LAY_measuredCategory)
        measuredValue = findViewById(R.id.tipDialog_LAY_measuredValue)
        optimalString = findViewById(R.id.tipDialog_LAY_optimalCategory)
        optimalValue = findViewById(R.id.tipDialog_LAY_optimalValue)
        recommandation = findViewById(R.id.tipDialog_LAY_recommendation)

        // Temperature is not analog, giving exact value
        if (tips.name == mContext.getString(R.string.temperature)) {
            measuredValue.text = tips.measuredValue
            optimalValue.text = tips.optimalValue
            recommandation.text = tips.recommendation
            measuredString.visibility = View.GONE
            optimalString.visibility = View.GONE
        } else {
            measuredString.text = tips.measuredString
            measuredValue.text = "(${tips.measuredValue})"
            optimalString.text = tips.optimalString
            optimalValue.text = tips.optimalValue
            recommandation.text = tips.recommendation
        }

        if (tips.alert) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                recommandation.setTextColor(mContext.getColor(R.color.red))
            }
        }
    }
}