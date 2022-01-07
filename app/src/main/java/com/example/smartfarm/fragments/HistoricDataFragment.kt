package com.example.smartfarm.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.smartfarm.MyAppClass.Constants.DAILY
import com.example.smartfarm.MyAppClass.Constants.HOURLY
import com.example.smartfarm.MyAppClass.Constants.INTERVAL
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.adapters.DataListAdapter
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.interfaces.MultipleDataCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.utils.CodingTools
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import androidx.activity.result.contract.ActivityResultContracts
import com.example.smartfarm.MyAppClass.Constants.STORAGE_PERMISSION
import com.example.smartfarm.utils.ParsingTools

import java.time.*
import android.app.Activity
import android.net.Uri

import androidx.activity.result.ActivityResultCallback

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.smartfarm.utils.CachedFileProvider
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception


/** This is a fragment that shows historic measured data according to the criteria.
 * the fragment will display the data currently in a recyclerview
 * //TODO: Add filters
 * //TODO: Find better way to present the data
 * type: Constant. Possibilities:
 * HOURLY = 0
 * DAILY = 1
 * WEEKLY = 2
 * MONTHLY = 3
 * INTERVAL = 4
 *
 * controller: The controller used for data collection
 * dateRange: a list of 2 longs that represent the start and end dates of data in UNIX time
 */
class HistoricDataFragment(
    mContext: Context,
    deviceId: String,
    type: Int,
    controller: DataController,
) : Fragment() {

    private val mContext = mContext
    private val type = type
    private val controller = controller
    private val deviceId = deviceId

    private lateinit var dataRecycler: RecyclerView
    private lateinit var emptyLbl: MaterialTextView
    private lateinit var loadingImage: LottieAnimationView
    private var dataList = arrayListOf<SmartFarmData>()

    private lateinit var fabMenu: FloatingActionMenu
    private lateinit var fabItemSave: FloatingActionButton
    private lateinit var fabItemShare: FloatingActionButton
    private lateinit var fabItemChart: FloatingActionButton

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                saveFileToDevice()
            } else {
                Log.d(TAG, "Denied")
                CodingTools.displayErrorDialog(
                    mContext,
                    getString(R.string.storage_permission_required_message)
                )
            }
        }

    private val directoryResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "uri: ${result.data}")
                val uri = result.data!!.data
                val fileOutputStream: FileOutputStream =
                    mContext.contentResolver.openOutputStream(uri!!) as FileOutputStream
                val textData = ParsingTools.convertDataToString(dataList)
                try {
                    fileOutputStream.use { fos ->
                        OutputStreamWriter(fos, Charsets.UTF_8).use { osw ->
                            BufferedWriter(osw).use { bf -> bf.write(textData) }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Problem: $e")
                }
            }
        }


    private val sendFileResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "uri: ${result.data}")
            } else {
                Log.d(TAG, "Code: ${result.resultCode} || ${result.data}")
            }
        }


    private fun saveFileToDevice() {
        Log.d(TAG, "saveFileToDevice: ")
        val fileName = "SF_${LocalDateTime.now().toString().replace('T', '_')}.csv"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/csv"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        directoryResultLauncher.launch(intent)
    }

    private fun updateDataList() {
        Log.d(TAG, "updateDataList: ")
        dataList.reverse()
        loadingImage.visibility = RelativeLayout.GONE
        if (dataList.isNotEmpty()) {
            emptyLbl.visibility = LinearLayout.GONE
            dataRecycler.visibility = LinearLayout.VISIBLE
            val adapter = DataListAdapter(
                requireContext(),
                dataList
            )
            dataRecycler.adapter = adapter
        } else {
            emptyLbl.visibility = LinearLayout.VISIBLE
            emptyLbl.text = getString(R.string.no_data_for_given_time)
            dataRecycler.visibility = LinearLayout.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Historic data fragment")
        val mView = inflater.inflate(R.layout.fragment_historic_data, container, false)
        initViews(mView)
        Log.d(TAG, "onCreateView: Creating new fragment: $type")
        displayData(type)
        return mView;
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews:  ")
        dataRecycler = mView.findViewById(R.id.historicData_LST_networkList)
        emptyLbl = mView.findViewById(R.id.historicData_LBL_title)
        loadingImage = mView.findViewById(R.id.historicData_IMG_lottieLoading)
        Log.d(TAG, "initViews: setting visible")
        loadingImage.visibility = ConstraintLayout.VISIBLE
        fabMenu = mView.findViewById(R.id.stats_FAB_menu)
        fabItemSave = mView.findViewById(R.id.stats_FAB_save)
        fabItemSave.setOnClickListener {
            if (dataList.isNotEmpty()) {
                checkStoragePermissions()
            } else {
                Toast.makeText(mContext, getString(R.string.no_data_to_save), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        fabItemShare = mView.findViewById(R.id.stats_FAB_share)
        fabItemShare.setOnClickListener {
            if (dataList.isNotEmpty()) {
                val fileName = "SF_${LocalDateTime.now().toString().replace('T', '_')}"
                shareData(fileName)
            } else {
                Toast.makeText(mContext, getString(R.string.no_data_to_share), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        fabItemChart = mView.findViewById(R.id.stats_FAB_chart)
        fabItemChart.setOnClickListener {
            chartData()
        }
    }


    /** This method will display the current data in a chart*/
    private fun chartData() {
        Log.d(TAG, "chartData: ")
    }

    /** This method will share the presented data*/
    private fun shareData(fileName: String) {
        Log.d(TAG, "shareData: ")
        val tempDir = mContext.cacheDir
        val tempFile = File.createTempFile(fileName, ".csv", tempDir)
        val fileOutputStream = FileOutputStream(tempFile)
        val data = ParsingTools.convertDataToString(dataList)
        try {
            fileOutputStream.use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { osw ->
                    BufferedWriter(osw).use { bf -> bf.write(data) }
                }
            }
            fileOutputStream.close()

            val mailIntent = Intent().putExtra(
                Intent.EXTRA_STREAM, Uri.parse(
                    "content://" +
                            CachedFileProvider.AUTHORITY + "/" + tempFile.name
                )
            ).setType("application/csv")
                .setAction(Intent.ACTION_SEND)
            try {
                Log.d(TAG, "shareData: Trying to send file $tempFile")
                sendFileResultLauncher.launch(mailIntent)
            } catch (e: Exception) {
                Log.d(TAG, "shareData: Problem: $e")
            }

        } catch (e: Exception) {
            Log.d(TAG, "Problem: $e")
        }
    }

    /** This method will save the presented data to the device*/
    private fun checkStoragePermissions() {
        Log.d(TAG, "saveDataToDevice:")
        if (CodingTools.checkPermission(
                requireActivity(),
                STORAGE_PERMISSION,
                requestPermissionLauncher
            )
        ) {
            saveFileToDevice()
        }
    }

    /** This method will display the data on the fragment according to the type given.
     * Hourly -> Displaying last 24 hours data
     * Daily -> Displaying daily average for last week (7 days)
     * Interval -> Displaying custom interval/specific date/time
     */
    private fun displayData(type: Int) {
        when (type) {
            HOURLY -> display24HoursData()
            DAILY -> displayDailyAverage()
            INTERVAL -> displayCustomData()
        }
    }

    /**Displaying custom interval/specific date/time*/
    private fun displayCustomData() {
        Log.d(TAG, "displayCustomData: ")
        emptyLbl.text = getString(R.string.pick_dates)
        loadingImage.visibility = ConstraintLayout.GONE
        emptyLbl.visibility = ConstraintLayout.VISIBLE
        val listener = object : MultipleDataCallback {
            override fun getData(data: ArrayList<SmartFarmData>?) {
                Log.d(TAG, "getData: Got data from range")
                if (data != null) {
                    dataList = data
                    updateDataList()
                } else {
                    CodingTools.displayErrorDialog(mContext, "No data was found")
                }
            }
        }


        val pickerFragment = DatePickerFragment(mContext, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) { // got some dates from it
                    Log.d(TAG, "result: got dates: $message")
                    val list = message.split("-")
                    val sDate = list[0]
                    val fDate = list[1]
                    Log.d(TAG, "Start: $sDate End: $fDate")
                    loadingImage.visibility = ConstraintLayout.VISIBLE
                    emptyLbl.visibility = ConstraintLayout.GONE
                    controller.getDataByDate(
                        deviceId,
                        Instant.ofEpochMilli(sDate.toLong()).atZone(ZoneId.systemDefault())
                            .toLocalDate(),
                        Instant.ofEpochMilli(fDate.toLong()).atZone(ZoneId.systemDefault())
                            .toLocalDate(),
                        listener
                    )
                } else { // got nothing, ignore

                }
            }
        })
        CodingTools.switchFragment(
            childFragmentManager,
            R.id.historicData_LAY_pickerFrame,
            pickerFragment,
            false,
            ""
        )
    }

    /**Displaying monthly average for last year (12 months)*/
    private fun displayMonthlyAverage() {
        Log.d(TAG, "displayMonthlyAverage: ")

    }

    /** This method will receive a data array and a date and will sum all the data of the same day*/
    private fun sumDailyData(day: String, data: ArrayList<SmartFarmData>): SmartFarmData {
        Log.d(TAG, "sumDailyData: ")
        val temp = SmartFarmData()
        var count = 0
        if (data.isNotEmpty()) {
            var tempHumidity = 0.0
            var tempSoil = 0
            var tempTemperature = 0.0
            var tempLight = 0
            var tempUV = 0
            temp.date = data[0].date
            temp.device = data[0].device
            for (item in data) {
                if (item.date == day) {
                    tempHumidity += item.humidity
                    tempSoil += item.soil
                    tempTemperature += item.temperature
                    tempLight += item.light
                    tempUV += item.uv
                    count++
                }
            }
            tempHumidity /= count
            tempSoil /= count
            tempTemperature /= count
            tempLight /= count
            tempUV /= count

            temp.humidity = tempHumidity
            temp.soil = tempSoil
            temp.temperature = tempTemperature
            temp.light = tempLight
            temp.uv = tempUV
        }
        return temp
    }

    /** Displaying weekly average for last month (4 weeks)*/
    private fun displayWeeklyAverage() {
        Log.d(TAG, "displayWeeklyAverage: ")


        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH)
        //        val startTest = LocalDate.parse("07/09/21", formatter)
        //        val endTest = LocalDate.parse("01/01/22", formatter)

        val currentDate = LocalDate.now()
        var tempDate = currentDate

        val dailyList = arrayListOf<LocalDate>()
        dailyList.add(currentDate)
        // get the last 4 weeks
        for (i in 1..27) {
            dailyList.add(tempDate.minusDays(i.toLong())) // take last 7 days
        }

        val startDate = dailyList[27]
        val endDate = dailyList[0]

        val week0 = arrayListOf<LocalDate>()
        val week1 = arrayListOf<LocalDate>()
        val week2 = arrayListOf<LocalDate>()
        val week3 = arrayListOf<LocalDate>()

        for (i in 0..6) {
            week0.add(dailyList[i])
        }

        for (i in 7..13) {
            week1.add(dailyList[i])
        }

        for (i in 14..20) {
            week2.add(dailyList[i])
        }

        for (i in 21..27) {
            week3.add(dailyList[i])
        }

        val listener = object : MultipleDataCallback {
            override fun getData(data: ArrayList<SmartFarmData>?) {
                Log.d(TAG, "getData: Got data from range")
                if (data != null) {
                    makeDailyAverages(dailyList, data)
                } else {
                    CodingTools.displayErrorDialog(mContext, "No data was found")
                }
            }
        }
        controller.getDataByDate(deviceId, startDate, endDate, listener)
    }

    /**Displaying daily average for last week (7 days)*/
    private fun displayDailyAverage() {
        Log.d(TAG, "displayDailyAverage: ")

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH)
        //        val startTest = LocalDate.parse("07/09/21", formatter)
        //        val endTest = LocalDate.parse("01/01/22", formatter)

        val currentDate = LocalDate.now()
        val dailyList = arrayListOf<LocalDate>()

        dailyList.add(currentDate)
        for (i in 1 until 7) {
            dailyList.add(currentDate.minusDays(i.toLong()))
        }

        val endDay = dailyList[0]
        val startDay = dailyList[6]


        val listener = object : MultipleDataCallback {
            override fun getData(data: ArrayList<SmartFarmData>?) {
                if (data != null) {
                    dataList = makeDailyAverages(dailyList, data)
                    updateDataList()
                } else {
                    CodingTools.displayErrorDialog(mContext, "No data was found")
                }
            }
        }
        controller.getDataByDate(deviceId, startDay, endDay, listener)
    }

    /** This method will receive an array of days to split the data by, and a raw
     * data array for the given date range.
     * the method will construct an array of 7 items which represent the daily data average
     * for the last 7 days
     */
    private fun makeDailyAverages(
        dailyList: ArrayList<LocalDate>,
        data: ArrayList<SmartFarmData>
    ): ArrayList<SmartFarmData> {
        Log.d(TAG, "makeDailyAverages: \n$dailyList\n$data")

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH)
        data.reverse()
        val dailyData = arrayListOf<SmartFarmData>()

        val day0 = arrayListOf<SmartFarmData>()
        val date0 = formatter.format(dailyList[0])
        for (item in data) {
            if (item.date == date0) {
                day0.add(item)
            }
        }
        dailyData.add(avgData(day0))

        val day1 = arrayListOf<SmartFarmData>()
        val date1 = formatter.format(dailyList[1])
        for (item in data) {
            if (item.date == date1) {
                day1.add(item)
            }
        }
        dailyData.add(avgData(day1))

        val day2 = arrayListOf<SmartFarmData>()
        val date2 = formatter.format(dailyList[2])
        for (item in data) {
            if (item.date == date2) {
                day2.add(item)
            }
        }
        dailyData.add(avgData(day2))

        val day3 = arrayListOf<SmartFarmData>()
        val date3 = formatter.format(dailyList[3])
        for (item in data) {
            if (item.date == date3) {
                day3.add(item)
            }
        }
        dailyData.add(avgData(day3))

        val day4 = arrayListOf<SmartFarmData>()
        val date4 = formatter.format(dailyList[4])
        for (item in data) {
            if (item.date == date4) {
                day4.add(item)
            }
        }
        dailyData.add(avgData(day4))

        val day5 = arrayListOf<SmartFarmData>()
        val date5 = formatter.format(dailyList[5])
        for (item in data) {
            if (item.date == date5) {
                day5.add(item)
            }
        }
        dailyData.add(avgData(day5))

        val day6 = arrayListOf<SmartFarmData>()
        val date6 = formatter.format(dailyList[6])
        for (item in data) {
            if (item.date == date6) {
                day6.add(item)
            }
        }
        dailyData.add(avgData(day6))
        dailyData.reverse()
        return dailyData
    }

    /** This method will receive a list of data object and will return a data object
     * that is the average of all the data objects
     */
    private fun avgData(data: ArrayList<SmartFarmData>): SmartFarmData {
        Log.d(TAG, "avgData: ")
        val temp = SmartFarmData()
        if (data.isNotEmpty()) {
            var tempHumidity = 0.0
            var tempSoil = 0
            var tempTemperature = 0.0
            var tempLight = 0
            var tempUV = 0
            temp.date = data[0].date
            temp.device = data[0].device
            for (item in data) {
                tempHumidity += item.humidity
                tempSoil += item.soil
                tempTemperature += item.temperature
                tempLight += item.light
                tempUV += item.uv
            }
            val size = data.size
            tempHumidity /= size
            tempSoil /= size
            tempTemperature /= size
            tempLight /= size
            tempUV /= size

            temp.humidity = tempHumidity
            temp.soil = tempSoil
            temp.temperature = tempTemperature
            temp.light = tempLight
            temp.uv = tempUV
        }
        return temp
    }

    /**Displaying last 24 hours data*/
    private fun display24HoursData() {
        Log.d(TAG, "display24HoursData: ")
        val currentDate = LocalDate.now()
        val yesterdayDate = LocalDate.now().minusDays(1)

        val currentTime = LocalTime.now()
        val yesterdayTime = LocalTime.now().minusHours(24)
        val myListener = object : MultipleDataCallback {
            override fun getData(data: ArrayList<SmartFarmData>?) {
                Log.d(TAG, "getData: Got data from range: $data")
                if (data != null) {
                    dataList = data
                    updateDataList()
                } else {
                    CodingTools.displayErrorDialog(mContext, "No data was found")
                }
            }
        }
        controller.getDataByDateAndTime(
            deviceId,
            yesterdayDate,
            currentDate,
            yesterdayTime,
            currentTime,
            myListener
        )
    }

}