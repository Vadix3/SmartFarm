package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.adapters.DeviceInfoAdapter
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmDevice
import com.google.android.material.textview.MaterialTextView

class DeviceListFragment(
    mContext: Context,
    devices: ArrayList<SmartFarmDevice>,
    title: String,
    listener: ResultListener
) : Fragment() {

    private val mContext = mContext
    private val devices = devices
    private val title = title
    private val listener = listener
    private lateinit var titleLbl: MaterialTextView
    private lateinit var deviceList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: DeviceListFragment")
        val mView = inflater.inflate(R.layout.fragment_info, container, false)
        initViews(mView)
        return mView;
    }

    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: View) {
        Log.d(MyAppClass.Constants.TAG, "initViews: DeviceListFragment")
        titleLbl = mView.findViewById(R.id.infoFragment_LBL_title)
        titleLbl.text = title
        deviceList = mView.findViewById(R.id.infoFragment_LST_recycler)
        val adapter = DeviceInfoAdapter(mContext, devices, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) {
                    Log.d(TAG, "result: Clicked on: ${devices[message.toInt()]}")
                }
            }
        })
        deviceList.adapter = adapter
    }
}