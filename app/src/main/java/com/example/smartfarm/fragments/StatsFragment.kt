package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.smartfarm.MyAppClass.Constants.DAILY
import com.example.smartfarm.MyAppClass.Constants.HOURLY
import com.example.smartfarm.MyAppClass.Constants.INTERVAL
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.activities.MainActivity
import com.example.smartfarm.adapters.HistoricDataViewPagerAdapter
import com.example.smartfarm.controllers.DataController
import com.google.android.material.tabs.TabLayout

class StatsFragment(mContext: Context, deviceId: String, controller: DataController) :
    Fragment() {

    private val mContext = mContext
    private val deviceId = deviceId
    private val controller = controller

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: StatsFragment")
        val mView = inflater.inflate(R.layout.fragment_stats, container, false)
        initViews(mView)
        return mView;
    }


    private fun initNavigationListener(mView: View) {
        Log.d(TAG, "initNavigationListener: ")
        tabLayout = mView.findViewById(R.id.stats_LAY_topTabs)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d(TAG, "onTabSelected: ${tab!!.text}")
                viewPager.currentItem = tab.position

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                Log.d(TAG, "onTabUnselected: ${tab!!.text}")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
//                Log.d(TAG, "onTabReselected: ${tab!!.text}")
            }
        })
    }


    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: HomeFragment")
        initNavigationListener(mView)
        initViewPager(mView)

    }


    private fun initViewPager(mView: View) {
        viewPager = mView.findViewById(R.id.stats_LAY_viewPager)
        val viewPagerAdapter =
            HistoricDataViewPagerAdapter(
                mContext,
                deviceId,
                controller,
                childFragmentManager,
                lifecycle
            )
        viewPager.adapter = viewPagerAdapter
        viewPager.offscreenPageLimit = 2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val tempActivity = requireActivity() as MainActivity

                when (position) {
                    HOURLY -> {
                        tabLayout.getTabAt(HOURLY)!!.select()
                        tempActivity.changeToolbarTitle(getString(R.string.last_24_hours))
                    }
                    DAILY -> {
                        tabLayout.getTabAt(DAILY)!!.select()
                        tempActivity.changeToolbarTitle(
                            getString(R.string.daily) + " " + getString(
                                R.string.average
                            )
                        )
                    }
                    INTERVAL -> {
                        tabLayout.getTabAt(INTERVAL)!!.select()
                        tempActivity.changeToolbarTitle(getString(R.string.custom))
                    }
                }
            }
        })
    }
}