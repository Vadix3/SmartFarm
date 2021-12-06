package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.CAMERA_PERMISSION
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.activities.MainActivity
import com.example.smartfarm.adapters.DeviceListAdapter
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.dialogs.NewDeviceDialog
import com.example.smartfarm.interfaces.*
import com.example.smartfarm.models.SmartFarmData
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig

class NetworkDetailsFragment(mContext: Context, network: SmartFarmNetwork) : Fragment() {

    val mContext = mContext;
    private val network: SmartFarmNetwork = network
    private val bundle = Bundle()
    private lateinit var dataController: DataController
    private var deviceList = arrayListOf<SmartFarmDevice>()// list of device objects
    private lateinit var addDeviceBtn: FloatingActionButton // fab to add devices
    private lateinit var deviceRecycler: RecyclerView  // recyclerView of the devices
    private lateinit var noDevicesText: MaterialTextView

    /** Permission listener result handler*/
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Granted")
                openScanner()
            } else {
                Log.d(TAG, "Denied")
                CodingTools.displayErrorDialog(
                    mContext,
                    mContext.getString(R.string.camera_permission_required_message)
                )
            }
        }

    /** QR scanner result handler*/
    private val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::handleResult)

    /** This is the method that will handle the result of the qr scanner*/
    private fun handleResult(myResult: QRResult) {
        Log.d(TAG, "handleResult: $myResult")
        var did = ""
        val text = when (myResult) {
            is QRResult.QRSuccess -> {
                did = myResult.content.rawValue
                checkQRValue(did)
            }
            QRResult.QRUserCanceled -> {
                Log.d(TAG, "handleResult: User canceled")
            }
            QRResult.QRMissingPermission -> "Missing permission"
            is QRResult.QRError -> "${myResult.exception.javaClass.simpleName}: ${myResult.exception.localizedMessage}"
        }
    }

    /** This method will check if the read id value is correct*/
    private fun checkQRValue(did: String) {
        Log.d(TAG, "checkQRValue: Checking: $did")
        if (did.length != 16) { // The length of the Pi serial number is 16 digit
            CodingTools.displayErrorDialog(mContext, resources.getString(R.string.wrong_id))
        } else {
            checkValueInServer(did)
        }
    }

    /** This method will check the probable did value in the server
     * if the value exists there are 2 options:
     * 1. the device is already activated, notify the user with the device details
     * 2. the device is not activate it yet, switch the activated value to true and move
     * to device configuration window
     */
    private fun checkValueInServer(did: String) {
        Log.d(TAG, "checkValueInServer: ")
        dataController.checkExistingDocument(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.DEVICES_COLLECTION,
            did,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "result: got result: $message")
                        if (did == message) {
                            registerNewDeviceDialog(did)
                        }
                    } else {
                        CodingTools.displayErrorDialog(mContext, message)
                    }
                }
            })
    }

    /** This method will open the new device dialog for the given did*/
    private fun registerNewDeviceDialog(did: String) {
        Log.d(TAG, "registerNewDeviceDialog: $did")
        val dialog = NewDeviceDialog(mContext, did, object : DeviceCallback {
            override fun getDevice(item: SmartFarmDevice) {
                Log.d(TAG, "getDevice: $item")
                dataController.initDevice(item, object : ResultListener {
                    override fun result(result: Boolean, message: String) {
                        if (result) {
                            Log.d(TAG, "result: SUCCESS: $message")
                            deviceList.add(item)
                            dataController.addDeviceToNetwork(
                                did,
                                network,
                                object : ResultListener {
                                    override fun result(result: Boolean, message: String) {
                                        if (result) {
                                            updateDevicesList()
                                        } else {
                                            CodingTools.displayErrorDialog(mContext, message)
                                        }
                                    }
                                })
                        } else {
                            CodingTools.displayErrorDialog(mContext, message)
                        }
                    }
                })
            }
        })
        CodingTools.openDialog(
            mContext,
            dialog,
            MyAppClass.Constants.WRAP_CONTENT,
            (mContext.resources.displayMetrics.widthPixels * 0.9).toInt(),
            0.9f
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Network details fragment")
        (requireActivity() as MainActivity).changeToolbarTitle(network.name)
        val mView = inflater.inflate(R.layout.fragment_network_details, container, false)
        initViews(mView)
        dataController = DataController(mContext)
        if (bundle.isEmpty) {
            loadFromCloud()
        } else {
            loadFromBundle()
        }
        return mView
    }

    /** This method will load the info from the bundle*/
    private fun loadFromBundle() {
        Log.d(TAG, "loadFromBundle: ")
        val value = bundle.get(MyAppClass.Constants.DEVICE_LIST) as String
        val turnsType = object : TypeToken<ArrayList<SmartFarmDevice>>() {}.type
        val devices = Gson().fromJson<ArrayList<SmartFarmDevice>>(value, turnsType)
        deviceList = devices
        updateDevicesList() // refresh the document list
    }

    /** This method will load the fragment details from the cloud*/
    private fun loadFromCloud() {
        Log.d(TAG, "loadFromCloud: ")
        dataController = DataController(mContext)
        fetchNetworksDevices()
    }

    /** This callback happens when we move make a transaction to another fragment
     * here we will save the fetched data to a bundle, to not load it from the server again
     * upon entering the fragment again
     */
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        val jsonList = Gson().toJson(deviceList)
        bundle.putSerializable(MyAppClass.Constants.DEVICE_LIST, jsonList)
    }

    /** This method will fetch all the devices that belong to this network*/
    private fun fetchNetworksDevices() {
        Log.d(TAG, "fetchUsersNetworks: ")
        dataController.fetchNetworkDevices(network, object : DeviceListCallback {
            override fun getDevices(result: Boolean, devices: ArrayList<SmartFarmDevice>?) {
                if (result) {
                    deviceList = devices!! // could be empty
                    if (devices.isNotEmpty()) {
                        noDevicesText.visibility = View.GONE
                    } else {
                        noDevicesText.visibility = View.VISIBLE
                    }
                    updateDevicesList() // refresh the document list
                } else {
                    CodingTools.displayToast(mContext, "Error fetching devices", Toast.LENGTH_SHORT)
                }
            }
        })
    }

    /** This method will refresh the devices list after update / init*/
    private fun updateDevicesList() {
        Log.d(TAG, "updateDevicesList: ")
        val adapter = DeviceListAdapter(
            requireContext(),
            deviceList,
            object : DeviceCallback {
                override fun getDevice(item: SmartFarmDevice) {
                    Log.d(TAG, "getDevice: clicked on: $item")
                    fetchDeviceDetails(item)
                }

            }
        )
        deviceRecycler.adapter = adapter
    }

    /** This method will fetch the device details from the server and then will open
     * the details fragment*/
    private fun fetchDeviceDetails(item: SmartFarmDevice) {
        Log.d(TAG, "openDeviceWindow: ")
        /** This method will fetch the most recent data from the devices measurement*/
        Log.d(TAG, "fetchLatestData: ")
        dataController.getLastEntry(item.did, object : MeasurementCallback {
            override fun getMeasurement(data: SmartFarmData?) {
                if (data != null) {
                    Log.d(TAG, "getMeasurement: $data")
                    openDetailsFragment(data, item)
                } else {
                    CodingTools.displayToast(
                        mContext,
                        "Error fetching device details",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        })
    }

    /** This method will open the device details fragment*/
    private fun openDetailsFragment(data: SmartFarmData, item: SmartFarmDevice) {
        Log.d(TAG, "openDetailsFragment: ")
        CodingTools.switchFragment(
            parentFragmentManager,
            R.id.main_LAY_mainFrame,
            DataFragment(mContext, data, item),
            true,
            "data"
        )
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: NetworkDetails")
        addDeviceBtn = mView.findViewById(R.id.networkDetails_BTN_addNetwork)
        // Once the user clicks the add device fab, a dialog with options will open
        // Using bluetooth or direct did
        addDeviceBtn.setOnClickListener {
            openAddDeviceDialog()
        }
        deviceRecycler = mView.findViewById(R.id.networkDetails_LST_networkList)
        noDevicesText = mView.findViewById(R.id.networkDetails_LBL_noDevices)
    }

    private fun openAddDeviceDialog() {
        Log.d(TAG, "openAddDeviceDialog: ")
        //TODO: Bug here
//        val dialog = AddDeviceModeDialog(mContext, object : ResultListener {
//            override fun result(result: Boolean, message: String) {
//                //Depends on the user's decision, the app will open the appropriate dialog
//                if (message == MyAppClass.Constants.BT_MODE) {
//                    openBluetoothMode()
//                } else {
//                    openQrMode()
//                }
//            }
//        })
//        CodingTools.openDialog(
//            mContext,
//            dialog,
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            (mContext.resources.displayMetrics.widthPixels * 0.9).toInt(),
//            0.9f
//        )
        checkCameraPermissions()
    }

    /** This method will check for camera permissions*/
    private fun checkCameraPermissions() {
        Log.d(TAG, "checkCameraPermissions: ")
        if (CodingTools.checkPermission(
                requireActivity(),
                CAMERA_PERMISSION,
                requestPermissionLauncher
            )
        ) {
            openScanner()
        }
    }

    /** This method will open the qr scanner and will get the read result*/
    private fun openScanner() {
        Log.d(TAG, "openScanner: ")
        scanCustomCode.launch(
            ScannerConfig.build {
                setBarcodeFormats(listOf(BarcodeFormat.FORMAT_ALL_FORMATS)) // set interested barcode formats
                setOverlayStringRes(R.string.scan_barcode) // string resource used for the scanner overlay
                setOverlayDrawableRes(R.drawable.ic_scan_barcode) // drawable resource used for the scanner overlay
                setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
                setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
            }
        )
    }

    /** This method will get the 'did' from a bluetooth search, once the connection is established
     */
    private fun openBluetoothMode() {
        Log.d(TAG, "openBluetoothMode: ")

    }
}