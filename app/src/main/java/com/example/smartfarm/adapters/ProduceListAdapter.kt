package com.example.smartfarm.adapters

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.ProduceRow
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList

class ProduceListAdapter(
    private val mContext: Context,
    private val resource: Int,
    private val items: List<ProduceRow>,
) : ArrayAdapter<ProduceRow>(mContext, resource, items) {

    var filtered = ArrayList<ProduceRow>()

    override fun getCount(): Int {
        return filtered.size
    }

    override fun getItem(position: Int): ProduceRow {
        Log.d(TAG, "getItem: ${filtered[position]}")
        return filtered[position]
    }

    override fun getItemId(position: Int): Long {
        var trueIndex = 0
        for (i in items.indices) {
            if (items[i].name == filtered[position].name) {
                trueIndex = i
                break
            }
        }
        Log.d(TAG, "getItemId: true index = $trueIndex")
        return trueIndex.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
        val view: View = layoutInflater.inflate(resource, parent, false)
        val item = filtered[position]
        val nameLbl: MaterialTextView = view.findViewById(R.id.produceRow_LBL_name)
        val iconImg: ShapeableImageView = view.findViewById(R.id.produceRow_IMG_icon)
        nameLbl.text = item.name
        iconImg.setImageResource(item.icon)
        return view
    }

    override fun getFilter() = filter

    private var filter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            val results = FilterResults()

            val query =
                if (constraint != null && constraint.isNotEmpty()) autocomplete(constraint.toString())
                else arrayListOf()

            results.values = query
            results.count = query.size

            return results
        }

        private fun autocomplete(input: String): ArrayList<ProduceRow> {
            val results = arrayListOf<ProduceRow>()

            for (specie in items) {
                if (specie.name.toLowerCase().startsWith(input.toLowerCase())) results.add(specie)
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
            filtered = results.values as ArrayList<ProduceRow>
            notifyDataSetInvalidated()
        }

        override fun convertResultToString(result: Any) = (result as ProduceRow).name
    }
}