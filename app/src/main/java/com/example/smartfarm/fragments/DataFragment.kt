package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.activities.MainActivity
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.CommandModel
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.imageview.ShapeableImageView

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
 * ========= NIGHT ============
 *
 * */
class DataFragment(mContext: Context, data: SmartFarmData, device: SmartFarmDevice) : Fragment() {

    val mContext = mContext;
    private val data: SmartFarmData = data
    private var device: SmartFarmDevice = device
    private lateinit var dataController: DataController

    private lateinit var settingsIcon: ShapeableImageView
    private lateinit var statsIcon: ShapeableImageView

    // Textviews - date, time, humidity, temp, soil, light, uv, title
    private lateinit var dateLbl: TextView
    private lateinit var timeLbl: TextView
    private lateinit var humidityLbl: TextView
    private lateinit var tempLbl: TextView
    private lateinit var soilLbl: TextView
    private lateinit var lightLbl: TextView
    private lateinit var uvLbl: TextView
    private lateinit var titleLbl: TextView

    // Icons
    private lateinit var dateImg: ShapeableImageView
    private lateinit var timeImg: ShapeableImageView
    private lateinit var humidityImg: ShapeableImageView
    private lateinit var tempImg: ShapeableImageView
    private lateinit var soilImg: ShapeableImageView
    private lateinit var lightImg: ShapeableImageView
    private lateinit var uvImg: ShapeableImageView
    private lateinit var titleImg: ShapeableImageView


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
        initViews(mView)
        getRecommendations()
        return mView;
    }

    /** This method will fetch growing recommendations according to the measured produce.
     *  recommendations regarding sunlight, water and temperature at this point.
     *  the method will check the recommended values and compare them to the measured ones,
     *  and will display a message to the user
     *  measured values:
     *
     */
    private fun getRecommendations() {
        val recommended = CodingTools.getSingleCropDetails(requireActivity(), device.produce)

    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")

        settingsIcon = mView.findViewById(R.id.data_IMG_settings)
        settingsIcon.setOnClickListener {
            openSettings()
        }
        statsIcon = mView.findViewById(R.id.data_IMG_stats)
        statsIcon.setOnClickListener {
            openStats()
        }

        //labels
        dateLbl = mView.findViewById(R.id.data_LBL_date)
        timeLbl = mView.findViewById(R.id.data_LBL_time)
        humidityLbl = mView.findViewById(R.id.data_LBL_humidity)
        tempLbl = mView.findViewById(R.id.data_LBL_temperature)
        soilLbl = mView.findViewById(R.id.data_LBL_soil)
        lightLbl = mView.findViewById(R.id.data_LBL_light)
        uvLbl = mView.findViewById(R.id.data_LBL_uv)
        titleLbl = mView.findViewById(R.id.data_LBL_title)

        //images
        dateImg = mView.findViewById(R.id.data_IMG_date)
        timeImg = mView.findViewById(R.id.data_IMG_time)
        humidityImg = mView.findViewById(R.id.data_IMG_humidity)
        tempImg = mView.findViewById(R.id.data_IMG_temperature)
        soilImg = mView.findViewById(R.id.data_IMG_soil)
        lightImg = mView.findViewById(R.id.data_IMG_light)
        uvImg = mView.findViewById(R.id.data_IMG_uv)

        //inject values
        dateLbl.text = resources.getString(R.string.date) + ": " + data.date
        timeLbl.text = resources.getString(R.string.time) + ": " + data.time
        humidityLbl.text =
            resources.getString(R.string.humidity) + ": " + String.format(
                "%.2f",
                data.humidity
            ) + "%"
        tempLbl.text =
            resources.getString(R.string.temperature) + ": " + String.format(
                "%.2f",
                data.temperature
            )
        soilLbl.text = resources.getString(R.string.soil_moisture) + ": " + data.soil.toString()
        lightLbl.text = resources.getString(R.string.light_exposure) + ": " + data.light.toString()
        uvLbl.text = resources.getString(R.string.uv_exposure) + ": " + data.uv.toString()
        titleLbl.text = device.name
    }

    /** This method will open the statistics page for the selected device*/
    private fun openStats() {
        Log.d(TAG, "openStats: ")
        val statsFragment = StatsFragment(mContext)
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
