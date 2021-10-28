package com.example.smartfarm.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.utils.CodingTools
import com.example.smartfarm.utils.ParsingTools
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.LocationCallback
import org.json.JSONObject


class HomeFragment(mContext: Context) : Fragment() {

    val mContext = mContext;

    // Google api location provider
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var weatherPreviewFragment: WeatherPreviewFragment
    private var shownLocationDialog = false // This variable will indicate if the user already saw
    private var isLoadingWeather = false
    // the location services message in order to not spam him

    /** Resolution result listener for location services*/
    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == AppCompatActivity.RESULT_OK) {
                Log.d(TAG, "User did activate: ")
                fetchLastLocation()
            } else {
                Log.d(TAG, "User did not activate: ")
                showEnableLocationBtn()
            }
        }

    /** Permission listener result handler*/
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Granted")
                if (locationEnabled()) {
                    fetchLastLocation()
                } else {
                    Log.d(TAG, "locationEnabled: location not enabled")
                    showLocationPrompt()
                }

            } else {
                Log.d(TAG, "Denied")
                showLocationPermissionMessage()
            }
        }

    /** This method will initialize the location services variables, most importantly the location
     * callback where we receive our desired location
     */
    private fun initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.interval = 200;
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "onLocationResult: null location")
                    return
                }
                for (location in locationResult.locations) {
                    val lat = location.latitude.toString()
                    val lon = location.longitude.toString()
                    Log.d(TAG, "onLocationResult: Got location: $lat,$lon")
                    fetchWeatherDetails(location)
                }
            }
        }
    }

    /** Since I already know that I have a permission here I don't need to check*/
    @SuppressLint("MissingPermission")
    /** This method will fetch the users last location and initiate the weather details method*/
    private fun fetchLastLocation() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    fetchWeatherDetails(location)
                    stopLocationUpdates()
                } else {
                    Log.d(TAG, "testMethod: Location is null")
                    showLoadingWeather()
                }
            }
    }

    /** This method will show that the weather info is loading*/
    private fun showLoadingWeather() {
        Log.d(TAG, "showLoadingWeather: ")
        if (!isLoadingWeather) {
            isLoadingWeather = true
            val message = getString(R.string.loading_weather)
            val drawableResource = CodingTools.getCircularDrawable(mContext)
            val callback = object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    Log.d(TAG, "result: click")
                }
            }
            val fragment = ErrorFragment(mContext, message, drawableResource, callback)
            CodingTools.switchFragment(
                childFragmentManager,
                R.id.home_LAY_weatherLayout,
                fragment,
                false,
                "Weather"
            )
        }
        fetchLastLocation()
    }

    /** This method will show a button that will offer the user the option to enable the location
     * services that he has previously declined to enable
     */
    private fun showEnableLocationBtn() {
        Log.d(TAG, "showEnableLocationBtn: ")
        if (!shownLocationDialog) {
            CodingTools.displayErrorDialog(
                mContext,
                mContext.getString(R.string.location_services_required)
            )
            shownLocationDialog = true
        }
        val message = getString(R.string.location_required)
        val drawableResource = mContext.getDrawable(R.drawable.ic_baseline_my_location_24)!!
        val callback = object : ResultListener {
            override fun result(result: Boolean, message: String) {
                showLocationPrompt()
            }
        }
        val fragment = ErrorFragment(mContext, message, drawableResource, callback)
        CodingTools.switchFragment(
            childFragmentManager,
            R.id.home_LAY_weatherLayout,
            fragment,
            false,
            "Weather"
        )


    }

    /** This method will present the user a message that says that location permissions are needed
     * for the weather feature
     */
    private fun showLocationPermissionMessage() {
        Log.d(TAG, "showLocationPermissionMessage: ")
        CodingTools.displayErrorDialog(
            mContext,
            mContext.getString(R.string.permission_required_message)
        )
        showLocationPermissionErrorFragment()
    }

    /** This method will show the weather fragment as an error fragment that will advise
     * the user to grant location permission
     */
    private fun showLocationPermissionErrorFragment() {
        Log.d(TAG, "showLocationPermissionErrorFragment: ")
        val message = getString(R.string.permission_missing)
        val drawableResource = mContext.getDrawable(R.drawable.ic_baseline_error_outline_24)!!
        val callback = object : ResultListener {
            override fun result(result: Boolean, message: String) {
                CodingTools.displayErrorDialog(
                    mContext,
                    getString(R.string.permission_required_message)
                )
            }
        }
        val fragment = ErrorFragment(mContext, message, drawableResource, callback)
        CodingTools.switchFragment(
            childFragmentManager,
            R.id.home_LAY_weatherLayout,
            fragment,
            false,
            "Weather"
        )
    }

    /** This method will prompt the user to activate the location services on the phone*/
    private fun showLocationPrompt() {
        Log.d(TAG, "showLocationPrompt: ")
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
                Log.d(TAG, "showLocationPrompt: satisfied")
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            // Cast to a resolvable exception.
                            val resolvable: ResolvableApiException =
                                exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            val intentSenderRequest =
                                IntentSenderRequest.Builder(exception.resolution).build()
                            resolutionForResult.launch(intentSenderRequest)

                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                            Log.d(TAG, "showLocationPrompt: here1")
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                            Log.d(TAG, "showLocationPrompt: here2")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        Log.d(TAG, "showLocationPrompt: Not satisfied")
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                    }
                }
            }
        }
    }

    /** A method to check if the location services are enabled*/
    private fun locationEnabled(): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /** A method to fetch the weather details for the given location*/
    private fun fetchWeatherDetails(location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        val key = CodingTools.getWeatherAPIKey(mContext)
        val link =
            "https://api.openweathermap.org/data/2.5/onecall" +
                    "?lat=$lat" +
                    "&lon=$lon" +
                    "&exclude=hourly,minutely" +
                    "&units=metric" +
                    "&appid=$key"
        val client = OkHttpClient()
        Thread {
            val request = Request.Builder().url(link).build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val weather = response.body!!.string()
                    Log.d(TAG, "testMethod: SUCCESS: $weather")
                    updateWeatherUI(weather)
                } else {
                    Log.d(TAG, "testMethod: RESPONSE ERROR: $response")
                    errorLoadingWeather(location)
                }

            } catch (e: Exception) {
                Log.d(TAG, "testMethod: ERROR: $e")
            }
        }.start()
    }

    /** This method will update the weather fragment with the given data*/
    private fun updateWeatherUI(weather: String) {
        Log.d(TAG, "updateWeatherUI: ")

        //Current weather
        val currentWeather = ParsingTools.parseWeather(JSONObject(weather))

        //Forecast list
        val dailyWeatherList = ParsingTools.parseDailyWeather(JSONObject(weather))

        weatherPreviewFragment = WeatherPreviewFragment(mContext, currentWeather, dailyWeatherList)


        CodingTools.switchFragment(
            childFragmentManager,
            R.id.home_LAY_weatherLayout,
            weatherPreviewFragment,
            false,
            "Weather"
        )
    }

    /** This method will show an error when a fetching error occurred*/
    private fun errorLoadingWeather(location: Location) {
        Log.d(TAG, "errorLoadingWeather: ")
        val message = getString(R.string.error_loading_weather)
        val drawableResource = mContext.getDrawable(R.drawable.ic_baseline_error_outline_24)!!
        val callback = object : ResultListener {
            override fun result(result: Boolean, message: String) {
                fetchWeatherDetails(location)
            }
        }
        val fragment = ErrorFragment(mContext, message, drawableResource, callback)
        CodingTools.switchFragment(
            childFragmentManager,
            R.id.home_LAY_weatherLayout,
            fragment,
            false,
            "Weather"
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: HomeFragment")
        val mView = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(mView)
        initLocationServices()
        tryToDisplayWeather()
//        updateWeatherUI(weatherExample)
        return mView;
    }

    /** This method will try to display the weather, depends on the location permissions
     * and location services on the phone. It will trigger the series of events to display the
     * weather to the user.
     * We check if the location permission has been granted. if we have the location
     * permission, we will check if the location services are enabled, if so, we will get the
     * last known location and fetch the weather report.
     * if the location services are not active, we will prompt the user to activate them.
     * if he refuses, we show a button where the user can activate the services and trigger
     * the chain again.
     * if the user did not give us location permissions, we will display a message to the user
     * that location permissions are needed for this feature
     * TODO: Find city by typing it in / from a list
     */
    private fun tryToDisplayWeather() {
        Log.d(TAG, "tryToDisplayWeather: ")
        if (CodingTools.checkPermission(
                requireActivity(),
                MyAppClass.Constants.LOCATION_PERMISSION, requestPermissionLauncher
            )
        ) {
            Log.d(TAG, "testMethod: Permission already given, continue")
            if (locationEnabled()) {
                fetchLastLocation()
            } else {
                Log.d(TAG, "locationEnabled: location not enabled")
                showLocationPrompt()
            }
        }
    }


    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: Any) {
        Log.d(TAG, "initViews: HomeFragment")


    }

    /** This method will stop the location requests*/
    private fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: ")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}