package com.example.smartfarm.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.WeatherReport
import com.google.android.material.card.MaterialCardView

class ErrorFragment(
    mContext: Context,
    message: String,
    imageResource: Drawable,
    callback: ResultListener
) : Fragment() {

    private val mContext = mContext
    private val message = message
    private val image = imageResource
    private val callback = callback

    private lateinit var messageLbl: TextView
    private lateinit var errorImg: ImageView
    private lateinit var mainLayout: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: HomeFragment")
        val mView = inflater.inflate(R.layout.fragment_error, container, false)
        initViews(mView)
        return mView;
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ErrorFragment")
        messageLbl = mView.findViewById(R.id.errorFragment_LBL_message)
        messageLbl.text = message
        errorImg = mView.findViewById(R.id.errorFragment_IMG_image)
        errorImg.setImageDrawable(image)
        mainLayout = mView.findViewById(R.id.errorFragment_LAY_cardview)
        mainLayout.setOnClickListener {
            // send the click event back to the caller who will know what to do since he created
            // the callback
            callback.result(true, "")
        }
    }
}