package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DatePickerFragment(
    mContext: Context,
    listener: ResultListener
) :
    Fragment() {

    private val mContext = mContext
    private val listener = listener

    private lateinit var pickerBtn: MaterialButton
    private lateinit var submitBtn: MaterialButton

    private var datesString = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: FragmentDatePicker")
        val mView = inflater.inflate(R.layout.fragment_date_picker, container, false)
        initViews(mView)
        return mView;
    }

    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: View) {
        Log.d(MyAppClass.Constants.TAG, "initViews: FragmentDatePicker")
        pickerBtn = mView.findViewById(R.id.picker_BTN_dates)
        pickerBtn.setOnClickListener {
            openCalendarPicker()
        }
        submitBtn = mView.findViewById(R.id.picker_BTN_submit)
        submitBtn.setOnClickListener {
            if (datesString == "") {
                listener.result(false, "")
            } else {
                listener.result(true, datesString)
            }
        }
    }

    private fun openCalendarPicker() {
        Log.d(TAG, "openCalendarPicker: ")

        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(getString(R.string.pick_dates))
                .setCalendarConstraints(
                    CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())
                        .build()
                )
                .build()

        dateRangePicker.show(childFragmentManager, "")
        dateRangePicker.addOnDismissListener {
            Log.d(TAG, "testMethod: user dismissed picker")
        }
        dateRangePicker.addOnCancelListener {
            Log.d(TAG, "openCalendarPicker: User cancel")
        }
        dateRangePicker.addOnNegativeButtonClickListener {
            Log.d(TAG, "openCalendarPicker: user negative button")
        }
        dateRangePicker.addOnPositiveButtonClickListener {
            Log.d(TAG, "openCalendarPicker: user positive button")
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH)


            val format = SimpleDateFormat("dd/MM/yy")
            val date1 = Date(dateRangePicker.selection!!.first)
            val date2 = Date(dateRangePicker.selection!!.second)

            val fromDate = format.format(date1)
            val toDate = format.format(date2)

            pickerBtn.text = "$fromDate - $toDate"
            datesString =
                "${dateRangePicker.selection!!.first}-${dateRangePicker.selection!!.second}"
        }
    }
}