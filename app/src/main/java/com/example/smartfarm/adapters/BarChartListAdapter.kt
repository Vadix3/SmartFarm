package com.example.smartfarm.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass.Constants.HUMIDITY_ID
import com.example.smartfarm.MyAppClass.Constants.LIGHT_ID
import com.example.smartfarm.MyAppClass.Constants.SOIL_ID
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.MyAppClass.Constants.TEMPERATURE_ID
import com.example.smartfarm.MyAppClass.Constants.UV_ID
import com.example.smartfarm.R
import com.example.smartfarm.models.MicroData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class BarChartListAdapter(
    context: Context,
    private val dataSet: ArrayList<ArrayList<MicroData>>
) :
    RecyclerView.Adapter<BarChartListAdapter.ViewHolder>() {

    private var mContext = context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        val mainLayout: MaterialCardView = v.findViewById(R.id.barChartRow_LAY_mainLayout)
        val titleLbl: MaterialTextView = v.findViewById(R.id.barChartRow_LBL_title)
        val barChart: BarChart = v.findViewById(R.id.barChartRow_LAY_chart)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_chart_bar, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Position:$position")
        val tempDataSet = dataSet[position]
        when (position) {
            HUMIDITY_ID -> {
                viewHolder.titleLbl.text = mContext.getText(R.string.humidity)
            }
            TEMPERATURE_ID -> {
                viewHolder.titleLbl.text = mContext.getText(R.string.temperature)
            }
            SOIL_ID -> {
                viewHolder.titleLbl.text = mContext.getText(R.string.soil_moisture)
            }
            LIGHT_ID -> {
                viewHolder.titleLbl.text = mContext.getText(R.string.light_exposure)
            }
            UV_ID -> {
                viewHolder.titleLbl.text = mContext.getText(R.string.uv_exposure)
            }
        }
        viewHolder.mainLayout.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: CLicked on: $position")
        }

        initBarChart(viewHolder.barChart, tempDataSet)

        //now draw bar chart with dynamic data
        val entries: ArrayList<BarEntry> = ArrayList()
        //you can replace this data object with  your custom object
        for (i in tempDataSet.indices) {
            val item = tempDataSet[i]
            entries.add(BarEntry(i.toFloat(), item.data.toFloat()))
        }
        Log.d(TAG, "chartData: Entries: $entries")
        val barDataSet = BarDataSet(entries, "")
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        val data = BarData(barDataSet)
        viewHolder.barChart.data = data
        viewHolder.barChart.invalidate()
    }


    inner class MyAxisFormatter(dataSet: ArrayList<MicroData>) : IndexAxisValueFormatter() {
        private val dataSet = dataSet
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < dataSet.size) {
                dataSet[index].date
            } else {
                ""
            }
        }
    }


    private fun initBarChart(barChart: BarChart, dataSet: ArrayList<MicroData>) {


//        hide grid lines
        barChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = barChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        barChart.axisRight.isEnabled = false

        //remove legend
        barChart.legend.isEnabled = false


        //remove description label
        barChart.description.isEnabled = false

        //add animation
        barChart.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.valueFormatter = MyAxisFormatter(dataSet)
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = +90f
    }


    override fun getItemCount() = dataSet.size
}