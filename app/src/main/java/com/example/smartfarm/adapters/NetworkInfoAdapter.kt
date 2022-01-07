package com.example.smartfarm.adapters

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import com.example.smartfarm.models.WeatherReport
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.sql.Date
import java.sql.Timestamp
import kotlin.math.roundToInt

class NetworkInfoAdapter(
    context: Context,
    private val dataSet: ArrayList<SmartFarmNetwork>,
    listener: ResultListener
) :
    RecyclerView.Adapter<NetworkInfoAdapter.ViewHolder>() {

    private var mContext = context
    private val listener = listener

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        val nameLbl: MaterialTextView = v.findViewById(R.id.deviceDetails_LBL_name)
        val descriptionLbl: MaterialTextView = v.findViewById(R.id.deviceDetails_LBL_description)
        val iconImg: ShapeableImageView = v.findViewById(R.id.deviceDetails_IMG_icon)
        val mainLayout: MaterialCardView = v.findViewById(R.id.deviceDetails_LAY_mainLayout)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_device_details, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        viewHolder.nameLbl.text = temp.name
//        val iconName = "${temp.produce.toLowerCase().replace(' ', '_')}"
//        Log.d(TAG, "iconName = $iconName")
//
//        val drawableResourceId: Int =
//            mContext.resources.getIdentifier(iconName, "drawable", mContext.packageName)

        viewHolder.iconImg.setImageResource(R.drawable.ic_baseline_network_on_24)
        viewHolder.mainLayout.setOnClickListener {
            listener.result(true, position.toString())
        }
    }

    override fun getItemCount() = dataSet.size
}