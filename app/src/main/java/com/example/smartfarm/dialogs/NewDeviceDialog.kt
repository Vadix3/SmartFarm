package com.example.smartfarm.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.adapters.ProduceListAdapter
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.models.ProduceRow
import com.example.smartfarm.models.SmartFarmDevice
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList

class NewDeviceDialog(mContext: Context, did: String, listener: DeviceCallback) : Dialog(mContext) {

    private val device = SmartFarmDevice()
    private val did = did
    private val resultListener = listener
    private val plainNames = context.resources.getStringArray(R.array.produce)
    private lateinit var submitBtn: MaterialButton
    private lateinit var nameLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var nameEdt: TextInputEditText
    private lateinit var descriptionEdt: TextInputEditText
    private lateinit var deviceId: MaterialTextView
    private lateinit var cropTypeLayout: TextInputLayout
    private lateinit var cropTypeList: AutoCompleteTextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_new_device)
        initViews()
    }

    private fun initViews() {
        Log.d(TAG, "initViews: ")
        nameLayout = findViewById(R.id.newDevice_LAY_nameLayout)
        descriptionLayout = findViewById(R.id.newDevice_LAY_descriptionLayout)
        nameEdt = findViewById(R.id.newDevice_EDT_name)
        descriptionEdt = findViewById(R.id.newDevice_EDT_details)
        deviceId = findViewById(R.id.newDevice_LAY_deviceId)
        deviceId.text = did

        cropTypeLayout = findViewById(R.id.newDevice_LAY_cropTypeLayout)
        cropTypeList = findViewById(R.id.newDevice_LAY_cropTypeText)
        val adapter =
            ProduceListAdapter(context, R.layout.row_produce_item, getCropsFromResources())
        cropTypeList.setAdapter(adapter)
        cropTypeList.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                device.produce = plainNames[id.toInt()]
            }

        }
        submitBtn = findViewById(R.id.newDevice_BTN_create)
        submitBtn.setOnClickListener {
            if (checkValidInput()) {
                Log.d(TAG, "initViews: name: ${nameEdt.text.toString()}")
                device.did = did
                device.name = nameEdt.text.toString()
                device.description = descriptionEdt.text.toString()
                device.active = true
                Log.d(TAG, "initViews: sending device: $device")
//                resultListener.getDevice(device)
//                dismiss()
            }
        }
    }

    /** This method will return an array of produceRow from the app resources
     * the method will get the names and extract the drawables png files*/
    private fun getCropsFromResources(): ArrayList<ProduceRow> {
        Log.d(TAG, "populateCropList: ")
        // A list of names that matches the produce names in the drawable
        plainNames.sort()
        val produceList = arrayListOf<ProduceRow>()
        for (item in plainNames) {
            val temp = ProduceRow()
            var drawableName = item
            temp.name = item

            drawableName = drawableName.toLowerCase()
            if (drawableName.contains(' ')) {
                drawableName = drawableName.replace(
                    ' ',
                    '_'
                ) // replace the spaces with _ to match the drawable names
            }
            Log.d(TAG, "getCropsFromResources: drawable name = $drawableName")

            val id = context.resources
                .getIdentifier(drawableName, "drawable", context.packageName) // get the id by name
            Log.d(TAG, "getCropsFromResources: id = $id")
            temp.icon = id
            produceList.add(temp)
        }
        return produceList
    }

    private fun checkValidInput(): Boolean {
        Log.d(TAG, "checkValidInput: ")
        if (nameEdt.text.toString().trim().isNotEmpty()) {
            nameLayout.error = null
        } else {
            nameLayout.error = "Please enter device name"
            nameEdt.text?.clear()
            return false
        }
        if (descriptionEdt.text.toString().trim().isNotEmpty()) {
            descriptionLayout.error = null
        } else {
            descriptionLayout.error = "Please enter device name"
            descriptionEdt.text?.clear()
            return false
        }
        if (cropTypeList.text.isEmpty()) {
            cropTypeLayout.error = context.resources.getString(R.string.produce)
        }
        return true
    }
}