package com.example.smartfarm.adapters

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.smartfarm.MyAppClass.Constants.DAILY
import com.example.smartfarm.MyAppClass.Constants.HOURLY
import com.example.smartfarm.MyAppClass.Constants.INTERVAL
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.fragments.HistoricDataFragment

private const val NUM_TABS = 3

public class HistoricDataViewPagerAdapter(
    mContext: Context,
    deviceId: String,
    controller: DataController,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val mContext = mContext
    private val controller = controller
    private val deviceId = deviceId

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {

        when (position) {
            HOURLY -> return HistoricDataFragment(mContext, deviceId, HOURLY, controller)
            DAILY -> return HistoricDataFragment(mContext, deviceId, DAILY, controller)
            INTERVAL -> return HistoricDataFragment(mContext, deviceId, INTERVAL, controller)
        }
        Log.d(TAG, "createFragment: returning default")
        return HistoricDataFragment(mContext, deviceId, HOURLY, controller)
    }
}