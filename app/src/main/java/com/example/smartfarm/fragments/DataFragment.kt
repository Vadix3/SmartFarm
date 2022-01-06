package com.example.smartfarm.fragments

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.data.DataFetcher
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.DATE
import com.example.smartfarm.MyAppClass.Constants.HUMIDITY
import com.example.smartfarm.MyAppClass.Constants.LIGHT_EXPOSURE
import com.example.smartfarm.MyAppClass.Constants.SOIL_MOISTURE
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.MyAppClass.Constants.TEMPERATURE
import com.example.smartfarm.MyAppClass.Constants.TIME
import com.example.smartfarm.MyAppClass.Constants.UV_EXPOSURE
import com.example.smartfarm.MyAppClass.Constants.WRAP_CONTENT
import com.example.smartfarm.R
import com.example.smartfarm.activities.MainActivity
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.dialogs.TipDialog
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.interfaces.MeasurementCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.CommandModel
import com.example.smartfarm.models.ProduceTip
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.utils.CodingTools
import com.example.smartfarm.utils.ParsingTools
import com.example.smartfarm.utils.ProduceTools
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/** This is a fragment that displays the given collected data
 *  The data shown:
 * Humidity in %
 * Temperature in C
 * soil moisture 0-700 +- (700 in water, 8 +- dry ground, 650 +- wet ground just watered,588,678 little drained after watering,
 * )
 * =========== DAY ============
 * light 0-1000+- (993-1010 direct sunlight,980- => small cloud, 970 cloud & shade, 59 evening, 27-33 more evening)
 * uv 0-100 (40-100 direct sunlight,0-30 +- shade,10-20 clouds in shade, 15-35 clouds,12-13 clouds and cover & evening. 7 more evening)
 *  ######### Check UV sensor wiring #########
 *
 * */
class DataFragment(mContext: Context, data: SmartFarmData, device: SmartFarmDevice) : Fragment() {

    val mContext = mContext;
    private var data: SmartFarmData = data
    private var device: SmartFarmDevice = device
    private lateinit var dataController: DataController

    private lateinit var settingsIcon: ShapeableImageView
    private lateinit var statsIcon: ShapeableImageView

    // A list of tips objects
    private lateinit var growingTips: ArrayList<ProduceTip>

    /** A list of fragments for the data
     * 7 fragments:
     * SOIL_MOISTURE = 0
     * TEMPERATURE = 1
     * LIGHT_EXPOSURE = 2
     * UV_EXPOSURE = 3
     * HUMIDITY = 4
     * DATE = 5
     * TIME = 6
     */
    private var fragmentsList = arrayListOf<FragmentDataCard>()

    private lateinit var readNewDataBtn: MaterialButton
    private lateinit var titleLbl: MaterialTextView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var loadingImg: ShapeableImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Device details fragment")
        // Change the toolbar title
        (requireActivity() as MainActivity).changeToolbarTitle(device.name)
        val mView = inflater.inflate(R.layout.fragment_data, container, false)
        dataController = DataController(mContext)
        checkRecommendations()
        initViews(mView)
        listenForDataChanges()
        return mView;
    }

    /** This method will initialize the data change listener*/
    private fun listenForDataChanges() {
        Log.d(TAG, "listenForDataChanges: ")
        dataController.listenForDataChanges(object : MeasurementCallback {
            override fun getMeasurement(newData: SmartFarmData?) {
                if (newData != null) { // if there is some data
                    data = newData // update new data
                    requireActivity().runOnUiThread {
                        updatePageUI()
                    }
                } else {
                    CodingTools.displayErrorDialog(
                        mContext,
                        getString(R.string.error_fetching_data)
                    )
                }
            }
        }, device.did)
    }

    /** This method will compare the current crop data and the recommended ones
     * and will display a '!' icon near the relevant measurement, meaning there is a recommendation
     * for this specific measurement.
     *
     */
    private fun checkRecommendations() {
        Log.d(TAG, "checkRecommendations: ")
        growingTips = arrayListOf()
        // All recommendations for the specific produce
        val recommended = ProduceTools.getSingleCropDetails(requireActivity(), device.produce)
        val waterTips = ProduceTools.getWaterTips(mContext, data.soil, recommended.water)
        growingTips.add(waterTips)
        val temperatureTips = ProduceTools.getTemperatureTips(
            mContext,
            data.temperature,
            recommended.minTemp,
            recommended.maxTemp
        )
        growingTips.add(temperatureTips)
        val lightTips = ProduceTools.getLightTips(mContext, data.light, recommended.sun)
        growingTips.add(lightTips)
        val uvTips = ProduceTools.getUVTips(mContext, data.uv, recommended.sun)
        growingTips.add(uvTips)

        val humidityTips = ProduceTip()
        growingTips.add(humidityTips)
    }


    /** This method will open a recommendation dialog according to data
     *
     * */
    private fun openRecommendations(key: Int) {
        Log.d(TAG, "openRecommendations: opening recommendations for: $key")
        var thisTip = ProduceTip()

        when (key) { // determine which tip is it
            HUMIDITY -> {
                thisTip = growingTips[HUMIDITY]
                thisTip.name = getString(R.string.humidity)
            }
            TEMPERATURE -> {
                thisTip = growingTips[TEMPERATURE]
                thisTip.name = getString(R.string.temperature)
            }
            SOIL_MOISTURE -> {
                thisTip = growingTips[SOIL_MOISTURE]
                thisTip.name = getString(R.string.soil_moisture)
            }
            LIGHT_EXPOSURE -> {
                thisTip = growingTips[LIGHT_EXPOSURE]
                thisTip.name = getString(R.string.light_exposure)
            }
            UV_EXPOSURE -> {
                thisTip = growingTips[UV_EXPOSURE]
                thisTip.name = getString(R.string.uv_exposure)
            }
        }
        val dialog = TipDialog(mContext, thisTip)
        CodingTools.openDialog(mContext, dialog, WRAP_CONTENT, WRAP_CONTENT, 0.8f)
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")
        swipeLayout = mView.findViewById(R.id.data_LAY_swipeLayout)
        swipeLayout.setOnRefreshListener {
            Log.d(TAG, "onRefresh: ")
            swipeLayout.isRefreshing = true
            readFreshData()
        }
        titleLbl = mView.findViewById(R.id.data_LBL_title)
        readNewDataBtn = mView.findViewById(R.id.data_BTN_readNewData)
        readNewDataBtn.setOnClickListener { readFreshData() }
        settingsIcon = mView.findViewById(R.id.data_IMG_settings)
        settingsIcon.setOnClickListener {
            openSettings()
        }
        statsIcon = mView.findViewById(R.id.data_IMG_stats)
        statsIcon.setOnClickListener {
            openStats()
        }

        loadingImg = mView.findViewById(R.id.data_IMG_loading)
        val circularProgressDrawable = CircularProgressDrawable(mContext)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        loadingImg.setImageDrawable(circularProgressDrawable)
        Log.d(TAG, "initViews: setting visible")
        loadingImg.visibility = ConstraintLayout.VISIBLE

        updatePageUI()
    }

    /** This method will read fresh data from the device and update the UI with the new data*/
    private fun readFreshData() {
        Log.d(TAG, "readFreshData: ")
        readNewDataBtn.isActivated = false
        val command = CommandModel()
        command.type = 2 // measure once
        command.data = System.currentTimeMillis().toString()
        command.deviceId = device.did
        command.id = UUID.randomUUID().toString()

        readNewDataBtn.text = ""
        val circularProgressDrawable = CircularProgressDrawable(mContext)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        readNewDataBtn.background = circularProgressDrawable
        readNewDataBtn.isActivated = false

        dataController.sendCommand(command, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) {
                    Log.d(TAG, "result: SUCCESS: $message")
                    swipeLayout.isRefreshing = false
                } else {
                    Log.d(TAG, "result: ERROR: $message")
                    CodingTools.displayErrorDialog(mContext, "Error: $message")
                }
            }
        })


    }

    /** This method will update the page UI to match the global data object*/
    private fun updatePageUI() {
        Log.d(TAG, "updatePageUI: ")
        checkRecommendations()
        loadingImg.visibility = ConstraintLayout.GONE
        readNewDataBtn.text = getText(R.string.read_new_data)
        readNewDataBtn.background = ColorDrawable(resources.getColor(R.color.colorPrimary))
        readNewDataBtn.isActivated = true


        /** A list of fragments for the data
         * 7 fragments:
         * SOIL_MOISTURE = 0
         * TEMPERATURE = 1
         * LIGHT_EXPOSURE = 2
         * UV_EXPOSURE = 3
         * HUMIDITY = 4
         * DATE = 5
         * TIME = 6
         */

        val tempList = arrayListOf<FragmentDataCard>()

        // ==============================  Soil 0  ================================

        var soilText = ""
        val soilCard = FragmentDataCard(
            mContext,
            SOIL_MOISTURE,
            R.drawable.soil,
            soilText,
            false,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        openRecommendations(message.toInt())
                    }
                }
            }) // 0

        if (growingTips[SOIL_MOISTURE].alert) { // check alert
            soilCard.warning = true
        }

        if (data.soil != -999) {
            soilText = resources.getString(R.string.soil_moisture) + ": " + data.soil.toString()
        } else {
            soilText = resources.getString(R.string.no_data)
        }
        soilCard.message = soilText

        CodingTools.switchFragment(childFragmentManager, R.id.data_LAY_soil, soilCard, false, "")

        // =========================================================================


        // ============================  Temperature 1 ================================

        var temperatureText = "" // *
        val temperatureCard = FragmentDataCard( // *
            mContext,
            TEMPERATURE,// *
            R.drawable.hot,// *
            temperatureText,// *
            false,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        openRecommendations(message.toInt())
                    }
                }
            }) // 0

        if (growingTips[TEMPERATURE].alert) { // check alert  // *
            temperatureCard.warning = true // *
        }

        if (data.temperature != -999.0) { // *
            temperatureText =
                resources.getString(R.string.temperature) + ": " + data.temperature.roundToInt() + "°C" // *
        } else {
            temperatureText = resources.getString(R.string.no_data) // *
        }
        temperatureCard.message = temperatureText // *
        CodingTools.switchFragment(
            childFragmentManager,
            R.id.data_LAY_temperature,
            temperatureCard,
            false,
            ""
        )

        // =========================================================================


        // ============================  Light 2 ================================

        var lightText = "" // *
        val lightCard = FragmentDataCard( // *
            mContext,
            LIGHT_EXPOSURE,// *
            R.drawable.sun,// *
            lightText,// *
            false,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        openRecommendations(message.toInt())
                    }
                }
            }) // 0

        if (growingTips[LIGHT_EXPOSURE].alert) { // check alert  // *
            lightCard.warning = true // *
        }

        if (data.light != -999) { // *
            lightText =
                resources.getString(R.string.light_exposure) + ": " + data.light.toString() // *
        } else {
            lightText = resources.getString(R.string.no_data) // *
        }
        lightCard.message = lightText // *
        CodingTools.switchFragment(childFragmentManager, R.id.data_LAY_light, lightCard, false, "")

        // =========================================================================

        // ============================  UV 3 ================================

        var uvText = "" // *
        val uvCard = FragmentDataCard( // *
            mContext,
            UV_EXPOSURE,// *
            R.drawable.uv,// *
            uvText,// *
            false,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        openRecommendations(message.toInt())
                    }
                }
            }) // 0

        if (growingTips[UV_EXPOSURE].alert) { // check alert  // *
            uvCard.warning = true // *
        }

        if (data.uv != -999) { // *
            uvText = resources.getString(R.string.uv_exposure) + ": " + data.uv.toString() // *
        } else {
            uvText = resources.getString(R.string.no_data) // *
        }
        uvCard.message = uvText // *
        CodingTools.switchFragment(childFragmentManager, R.id.data_LAY_uv, uvCard, false, "")

        // =========================================================================


        // ============================  Humidity 4 ================================

        var humidityText = "" // *
        val humidityCard = FragmentDataCard( // *
            mContext,
            HUMIDITY,// *
            R.drawable.humidity,// *
            humidityText,// *
            false,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "result: humidity click")
                    }
                }
            }) // 0

        if (growingTips[HUMIDITY].alert) { // check alert  // *
            humidityCard.warning = true // *
        }

        if (data.humidity != -999.0) { // *
            humidityText =
                resources.getString(R.string.humidity) + ": " + data.humidity.roundToInt() + "%" // *
        } else {
            humidityText = resources.getString(R.string.no_data) // *
        }
        humidityCard.message = humidityText // *
        CodingTools.switchFragment(
            childFragmentManager,
            R.id.data_LAY_humidity,
            humidityCard,
            false,
            ""
        )

        // =========================================================================


        // ============================  Date 5 ================================

        var dateText = resources.getString(R.string.date) + ": " + data.date // *

        val dateCard = FragmentDataCard( // *
            mContext,
            DATE,// *
            R.drawable.calendar,// *
            dateText,// *
            false,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "result: Date click")
                    }
                }
            })
        CodingTools.switchFragment(childFragmentManager, R.id.data_LAY_date, dateCard, false, "")

        // =========================================================================


        // ============================  Time 6 ================================

        var timeText = resources.getString(R.string.time) + ": " + data.time

        val timeCard = FragmentDataCard( // *
            mContext,
            TIME,// *
            R.drawable.clock,// *
            timeText,// *
            false,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "result: time click")
                    }
                }
            })

        CodingTools.switchFragment(childFragmentManager, R.id.data_LAY_time, timeCard, false, "")

        // =========================================================================

        fragmentsList = tempList
        titleLbl.text = device.name
        swipeLayout.isRefreshing = false
    }


    /** This method will open the statistics page for the selected device*/
    private fun openStats() {
        Log.d(TAG, "openStats: ")
        val statsFragment = StatsFragment(mContext, device.did, dataController)
        CodingTools.switchFragment(
            parentFragmentManager,
            R.id.main_LAY_mainFrame,
            statsFragment,
            true,
            "Open_stats"
        )
    }

    /** This method will be executed once the user has returned from settings by pressing
     * the submit button on the settings page. There might be some changes made in the
     * device settings, so the method will compare the given device with the current device,
     * and if there are any changes made in the configuration, it will update them in the cloud
     * and send the commands to the device accordingly
     */
    private fun backFromSettings(newDevice: SmartFarmDevice) {
        Log.d(TAG, "backFromSettings: $newDevice")
        if (sameDevices(newDevice)) {// No changes should be made
            Log.d(TAG, "backFromSettings: Devices are same")
        } else { // changes were made, need to update document
            Log.d(TAG, "backFromSettings: devices not same")
            updateDeviceInCloud(newDevice)
        }
    }

    /** This method will update the device document in the cloud*/
    private fun updateDeviceInCloud(newDevice: SmartFarmDevice) {
        Log.d(TAG, "updateDeviceInCloud: ")
        val listener = object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) {
                    // In this case the device was successfully updated in the cloud
                    Log.d(TAG, "result: SUCCESS: $message")
                    device.name = newDevice.name
                    device.description = newDevice.description
                    device.produce = newDevice.produce

                    checkForCommands(newDevice)
                } else {
                    Log.d(TAG, "result: ERROR: $message")
                    // Some error occurred, display the user a dialog with the error
                    CodingTools.displayErrorDialog(
                        mContext,
                        getString(R.string.error_updating_device) + message
                    )
                }
            }
        }
        dataController.updateDevice(newDevice, listener)
    }

    /** This method will use the new device config from the settings and will
     * compare it to the current one to check if any new commands should be given.
     * Possible commands:
     * - Change measurement interval
     */
    private fun checkForCommands(newDevice: SmartFarmDevice) {
        Log.d(TAG, "checkForCommands: ")

        //Check for interval change command
        if (newDevice.measure_interval != device.measure_interval) {
            sendIntervalCommand(newDevice)
        }
        //Check for switch device on/off command
        if (newDevice.active != device.active) {
            sendSwitchCommand(newDevice)
        }
    }

    /** This method will send a 'switch' command to the cloud*/
    private fun sendSwitchCommand(newDevice: SmartFarmDevice) {
        Log.d(TAG, "sendSwitchCommand: ")
        val listener = object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) { // command was issued successfully, update the device
                    Log.d(TAG, "result: SUCCESS: $message")
                    device.active = newDevice.active
                } else {
                    Log.d(TAG, "result: ERROR: $message")
                    CodingTools.displayErrorDialog(
                        mContext,
                        getString(R.string.error_changing_switch) + message
                    )
                }
            }
        }
        val command = CommandModel()
        command.deviceId = device.did
        command.time = System.currentTimeMillis()
        command.type = MyAppClass.Constants.SWITCH_COMMAND
        command.data = newDevice.active.toString()
        dataController.sendCommand(command, listener)
    }

    /** This method will send a 'change interval' command to the cloud*/
    private fun sendIntervalCommand(newDevice: SmartFarmDevice) {
        Log.d(TAG, "sendIntervalCommand: ")
        val listener = object : ResultListener {
            override fun result(result: Boolean, message: String) {
                if (result) { // command was issued successfully, update the device
                    Log.d(TAG, "result: SUCCESS: $message")
                    device.measure_interval = newDevice.measure_interval
                } else {
                    Log.d(TAG, "result: ERROR: $message")
                    CodingTools.displayErrorDialog(
                        mContext,
                        getString(R.string.error_changing_interval) + message
                    )
                }
            }
        }
        val command = CommandModel()
        command.deviceId = device.did
        command.time = System.currentTimeMillis()
        command.type = MyAppClass.Constants.INTERVAL_COMMAND
        command.data = newDevice.measure_interval.toString()
        dataController.sendCommand(command, listener)
    }

    /** This method will compare a given device to the current fragment device*/
    private fun sameDevices(newDevice: SmartFarmDevice): Boolean {
        Log.d(TAG, "sameDevices: ")
        if (device.name != newDevice.name) {
            Log.d(TAG, "sameDevices: name diff")
            return false
        }
        if (device.active != newDevice.active) {
            Log.d(TAG, "sameDevices: Active diff")
            return false
        }
        if (device.produce != newDevice.produce) {
            Log.d(TAG, "sameDevices: produce diff")
            return false
        }
        if (device.measure_interval != newDevice.measure_interval) {
            Log.d(TAG, "sameDevices: measure diff")
            return false
        }
        if (device.description != newDevice.description) {
            Log.d(TAG, "sameDevices: desc diff")
            return false
        }
        return true
    }

    /** This method will open the settings page for the selected device*/
    private fun openSettings() {
        Log.d(TAG, "openSettings: ")
        val listener = object : DeviceCallback {
            override fun getDevice(item: SmartFarmDevice) {
                backFromSettings(item)
            }
        }
        val settingsFragment = DeviceSettingsFragment(mContext, device, listener)
        CodingTools.switchFragment(
            parentFragmentManager,
            R.id.main_LAY_mainFrame,
            settingsFragment,
            true,
            "Open_settings"
        )
    }
}
