package com.example.smartfarm.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.NetworkCallback
import com.example.smartfarm.models.SmartFarmNetwork
import com.google.android.material.card.MaterialCardView

class NetworkListAdapter(
    context: Context,
    private val dataSet: ArrayList<SmartFarmNetwork>,
    private val listener: NetworkCallback
) :
    RecyclerView.Adapter<NetworkListAdapter.ViewHolder>() {

    private var context: Context = context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        val name:TextView = v.findViewById(R.id.networkRow_LBL_networkName)
        val mainLayout:MaterialCardView = v.findViewById(R.id.networkRow_LAY_mainLayout)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.network_row, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        viewHolder.name.text = temp.name
        viewHolder.mainLayout.setOnClickListener{
            listener.getNetwork(temp)
        }
    }

    override fun getItemCount() = dataSet.size
}