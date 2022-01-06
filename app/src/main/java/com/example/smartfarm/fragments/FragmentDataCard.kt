package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class FragmentDataCard(
    mContext: Context,
    type: Int,
    icon: Int,
    message: String,
    warning: Boolean,
    listener: ResultListener
) :
    Fragment() {

    val mContext = mContext
    val type = type
    val icon = icon
    var message = message
    val listener = listener
    var warning = warning

    private lateinit var mainCard: MaterialCardView
    private lateinit var iconImg: AppCompatImageView
    private lateinit var messageLbl: MaterialTextView
    private lateinit var infoImg: ShapeableImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: FragmentDataCard")
        val mView = inflater.inflate(R.layout.fragment_data_row, container, false)
        initViews(mView)
        return mView;
    }

    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: View) {
        Log.d(MyAppClass.Constants.TAG, "initViews: FragmentDataCard")
        mainCard = mView.findViewById(R.id.dataCard_LAY_mainCard)
        mainCard.setOnClickListener {
            listener.result(true, type.toString())
        }
        iconImg = mView.findViewById(R.id.dataCard_IMG_icon)
        iconImg.setImageResource(icon)
        messageLbl = mView.findViewById(R.id.dataCard_LBL_dataString)
        messageLbl.text = message
        infoImg = mView.findViewById(R.id.dataCard_IMG_dataInfo)
        if (warning) {
            infoImg.setImageResource(R.drawable.ic_baseline_info_24_red)
        } else {
            infoImg.setImageResource(R.drawable.ic_baseline_info_24)
        }
        if (type == MyAppClass.Constants.DATE || type == MyAppClass.Constants.TIME || type == MyAppClass.Constants.HUMIDITY) {
            infoImg.visibility = RelativeLayout.GONE
        }
    }
}