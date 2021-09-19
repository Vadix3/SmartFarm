package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R

class HomeFragment(mContext: Context): Fragment() {

    val mContext=mContext;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: HomeFragment")
        val mView = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(mView)
        return mView;
    }

    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: Any) {
        Log.d(TAG, "initViews: HomeFragment")


    }
}