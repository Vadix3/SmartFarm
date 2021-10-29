package com.example.smartfarm.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.CAMERA_PERMISSION
import com.example.smartfarm.MyAppClass.Constants.LOCATION_PERMISSION
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.models.CropDetails
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/** A class of usefull coding tools*/

object CodingTools {


    /** A method to place the given fragment int the given frame, given a fragment manager.
     * can be added to backstack if needed, with given name
     */
    fun switchFragment(
        fm: FragmentManager,
        srcFrame: Int,
        targetFragment: Fragment,
        addToBackStack: Boolean,
        transactionName: String
    ) {
        val transaction = fm.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        transaction.replace(srcFrame, targetFragment)
        if (addToBackStack) {
            transaction.addToBackStack(transactionName)
        }
        transaction.commit()
    }

    /** A method to display a toast with given message and length*/
    fun displayToast(myContext: Context, message: String, length: Int) {
        Toast.makeText(myContext, message, length).show()
    }

    /** This method will display an error dialog with the given message*/
    fun displayErrorDialog(myContext: Context, message: String) {
        val builder = AlertDialog.Builder(myContext)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }.create().show()
    }


    /** This method will present the new network dialog, and will return with the result of the
     * creation
     */
    fun openDialog(context: Context, dialog: Dialog, height: Int, width: Int, dim: Float) {
        Log.d(MyAppClass.Constants.TAG, "createNewNetwork: ")
        dialog.show()
        dialog.window!!.setLayout(width, height)
        dialog.window!!.setDimAmount(dim)
    }


    /** This method will retutn a json object read from the local assets*/
    fun loadJSONFromAsset(activity: Activity, fileName: String): JSONObject? {
        var json: JSONObject
        try {
            val inputStream = activity.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = JSONObject(String(buffer))
        } catch (ex: IOException) {
            Log.d(TAG, "ERROR: $ex")
            return null
        }
        return json
    }

    /** This method will fetch a single crop details object from the array using the name*/
    fun getSingleCropDetails(activity: Activity, name: String): CropDetails {
        val tempObject = CropDetails()
        val data = loadJSONFromAsset(activity, "crop_details.json")
        if (data != null) {
            val cropArray = data.get("crops") as JSONArray
            for (i in 0 until cropArray.length()) {
                val item = cropArray.get(i) as JSONObject
                if (item.get("name") == name) {
                    val tips = item.get("tips") as JSONObject
                    tempObject.id = item.get("id") as String
                    tempObject.name = item.get("name") as String
                    tempObject.sun = tips.get("sun") as String
                    tempObject.water = tips.get("water") as String
                    tempObject.minTemp = tips.get("min_temp") as Int
                    tempObject.maxTemp = tips.get("max_temp") as Int
                }
            }
        }
        return tempObject
    }

    fun getWeatherAPIKey(context: Context): String {
        val ai: ApplicationInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["weatherKey"]

        return value.toString()
    }

    /** This method will check if the requested permission is given.
     * if so it will send the proper response through the requestPermissionLauncher,
     * or true if it was already granted
     */
    fun checkPermission(
        activity: Activity,
        permission: Int,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ): Boolean {
        var permissionCode = ""
        when (permission) { // get proper permission code
            CAMERA_PERMISSION -> {
                permissionCode = Manifest.permission.CAMERA
            }
            LOCATION_PERMISSION -> {
                permissionCode = Manifest.permission.ACCESS_COARSE_LOCATION
            }
        }
        when {
            ContextCompat.checkSelfPermission(
                activity,
                permissionCode
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted
                Log.d(TAG, "checkPermission: Permission already granted")
                return true
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permissionCode
            ) -> {
                Log.d(TAG, "checkPermission: permission required")
                requestPermissionLauncher.launch(
                    permissionCode
                )
            }
            else -> {
                Log.d(TAG, "checkPermission: asking permission")
                requestPermissionLauncher.launch(
                    permissionCode
                )
            }
        }
        return false
    }

    // Day
    fun getDayOfWeek(timestamp: Long): String {
        return SimpleDateFormat("EEEE", Locale.ENGLISH).format(timestamp * 1000)
    }

    // Month
    fun getMonthFromTimeStamp(timestamp: Long): String {
        return SimpleDateFormat("MMM", Locale.ENGLISH).format(timestamp * 1000)
    }

    // Year
    fun getYearFromTimeStamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy", Locale.ENGLISH).format(timestamp * 1000)
    }

    // (WED-MAY-2021)
    fun getFullDayOfWeek(timestamp: Long): String {
        return SimpleDateFormat("EEEE-MMM-yyyy", Locale.ENGLISH).format(timestamp * 1000)
    }

    // (WED-MAY)
    fun getDayMonthOfWeek(timestamp: Long): String {
        return SimpleDateFormat("EEEE-MMM", Locale.ENGLISH).format(timestamp * 1000)
    }

    //hh:mm

    fun getHourAndMinute(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.ENGLISH).format(timestamp * 1000)
    }

    fun getCircularDrawable(mContext: Context): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(mContext)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }
}