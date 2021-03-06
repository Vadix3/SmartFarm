package com.example.smartfarm.activities


import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.util.Pair
import androidx.core.view.isVisible
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.fragments.*
import com.example.smartfarm.interfaces.DeviceCallback
import com.example.smartfarm.interfaces.LoginListener
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import com.example.smartfarm.utils.CodingTools
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var headToolbar: Toolbar // Top toolbar

    private lateinit var homeFragment: HomeFragment
    private lateinit var networksFragment: NetworksFragment
    private lateinit var settingsFragment: SettingsFragment

    private var isHomeFragment: Boolean = true // A flag that indicates if home fragment is visible


    /** TESTING */
    //TODO: make animations direction wise

    /** This method will change the title of the global toolbar using the given text*/
    fun changeToolbarTitle(title: String) {
        Log.d(TAG, "changeToolbarTitle: ")
        headToolbar.title = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        testMethod()
        initViews()
        initLoginSequence()
    }

    private fun testMethod() {


    }


    /** A method to perform the login sequence:
     * 1. The login fragment will receive the email and password
     *    if the login was successful, the program will continue from the listener provided here.
     * 2. The user will sign up
     *    after a successful sign up, the program will continue from here without the need to login.
     * */
    private fun initLoginSequence() {
        Log.d(TAG, "initLoginSequence: ")
        CodingTools.switchFragment(
            supportFragmentManager,
            R.id.main_LAY_mainFrame,
            LoginFragment(this, object : LoginListener {
                /** After the user logged in, the program will update the UI from here*/
                override fun moveToMain(email: String) {
                    MyAppClass.Constants.USER_EMAIL = email // Save the users email for future use
                    runOnUiThread {
                        initFragments()
                        initBottomBar()
                    }
                }
            }),
            false,
            ""
        )
    }

    /** A method to initialize the Main fragments*/
    private fun initFragments() {
        Log.d(TAG, "initFragments: MainActivity")
        homeFragment = HomeFragment(this)
        CodingTools.switchFragment(
            supportFragmentManager,
            R.id.main_LAY_mainFrame,
            homeFragment,
            false,
            ""
        )
        networksFragment = NetworksFragment(this)
        settingsFragment = SettingsFragment(this)
    }


    /** A method to initialize the bottom navigation bar behaviour*/
    fun initBottomBar() {
        Log.d(TAG, "initBottomBar: ")
        navBar = findViewById(R.id.main_bottom_navigation)
        navBar.isVisible = true
        headToolbar.isVisible = true
        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_menu_home -> {
                    if (item.itemId != navBar.selectedItemId) { // if the item is not already selected check
                        Log.d(TAG, "bar listener: home click")
                        setHomeFragmentUI()
                    }
                    false
                }
                R.id.bottom_menu_network -> {
                    if (item.itemId != navBar.selectedItemId) { // if the item is not already selected check
                        Log.d(TAG, "bar listener: networks click")
                        setNetworksFragmentUI()
                    }
                    false
                }
                else -> true
            }
            true
        }
    }

    /** A method to initialize the UI components of the main activity*/
    private fun initViews() {
        Log.d(TAG, "initViews: MainActivity")
        headToolbar = findViewById(R.id.main_TLB_head)
        headToolbar.isVisible = false
        headToolbar.title = getString(R.string.home)
    }


    /** The function overrides the default onBackPressed method and first sends the user to the
     * home fragment, and if he's already on the home fragment then the behaviour is default
     */
    override fun onBackPressed() {
//        if (!isHomeFragment) {
//            setHomeFragmentUI()
//        } else {
        super.onBackPressed()
//        }
    }


    /** These methods will update the UI according to the selected fragment*/

    private fun setHomeFragmentUI() {
        Log.d(TAG, "setHomeFragmentUI: ")
        headToolbar.title = getString(R.string.home)
        CodingTools.switchFragment(
            supportFragmentManager,
            R.id.main_LAY_mainFrame,
            homeFragment,
            false,
            ""
        )
        isHomeFragment = true
    }

    private fun setNetworksFragmentUI() {
        Log.d(TAG, "setNetworksFragmentUI: ")
        headToolbar.title = getString(R.string.networks)
        CodingTools.switchFragment(
            supportFragmentManager,
            R.id.main_LAY_mainFrame,
            networksFragment,
            false,
            ""
        )
        isHomeFragment = false
    }

    private fun setSettingsFragmentUI() {
        Log.d(TAG, "setHomeFragmentUI: ")
        CodingTools.switchFragment(
            supportFragmentManager,
            R.id.main_LAY_mainFrame,
            settingsFragment,
            false,
            ""
        )
        headToolbar.title = getString(R.string.settings)
        isHomeFragment = false
    }

    /** ===================================================================*/

}