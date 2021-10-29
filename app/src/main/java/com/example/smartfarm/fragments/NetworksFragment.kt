package com.example.smartfarm.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.NETWORK_LIST
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.adapters.NetworkListAdapter
import com.example.smartfarm.controllers.DataController
import com.example.smartfarm.dialogs.FirstNetworkDialog
import com.example.smartfarm.dialogs.NewNetworkDialog
import com.example.smartfarm.interfaces.DeviceListCallback
import com.example.smartfarm.interfaces.NetworkListCallback
import com.example.smartfarm.interfaces.NetworkCallback
import com.example.smartfarm.interfaces.ResultListener
import com.example.smartfarm.models.SmartFarmDevice
import com.example.smartfarm.models.SmartFarmNetwork
import com.example.smartfarm.utils.CodingTools
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class NetworksFragment(mContext: Context) : Fragment() {

    private val mContext = mContext;
    private val savedState = Bundle()
    private lateinit var dataController: DataController
    private var networkList = arrayListOf<SmartFarmNetwork>()// list of network object
    private lateinit var createNetworkBtn: FloatingActionButton // fab to add networks
    private lateinit var networkRecycler: RecyclerView  // recyclerView of the networks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: NetworksFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: NetworksFragment")

        /** here we check if we already loaded this fragment, and therefore the bundle should
        not be empty.
        instead we will load the information from the bundle instead of the cloud*/
        val mView = inflater.inflate(R.layout.fragment_networks, container, false)
        initViews(mView)
        if (savedState.isEmpty) {
            loadFromCloud()
        } else {
            loadFromBundle()
        }
        return mView;
    }

    /** This method will load the info from the bundle*/
    private fun loadFromBundle() {
        Log.d(TAG, "loadFromBundle: ")
        val value = savedState.get(NETWORK_LIST) as String
        val turnsType = object : TypeToken<ArrayList<SmartFarmNetwork>>() {}.type
        val turns = Gson().fromJson<ArrayList<SmartFarmNetwork>>(value, turnsType)
        networkList = turns
        updateNetworkList()
    }

    /** This method will load the fragment details from the cloud*/
    private fun loadFromCloud() {
        Log.d(TAG, "loadFromCloud: ")
        dataController = DataController(mContext)
        fetchUsersNetworks()
    }

    /** This callback happens when we move make a transaction to another fragment
     * here we will save the fetched data to a bundle, to not load it from the server again
     * upon entering the fragment again
     */
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        val jsonList = Gson().toJson(networkList)
        savedState.putSerializable(NETWORK_LIST, jsonList)
    }

    /** This method will fetch all the users networks from the server
     * if no networks are found, a prompt will be shown to create a new device network
     */
    private fun fetchUsersNetworks() {
        Log.d(TAG, "fetchUsersNetworks: ")
        dataController.fetchUsersNetworks(object : NetworkListCallback {
            override fun getNetworks(result: Boolean, networks: ArrayList<SmartFarmNetwork>?) {
                if (result) {
                    networkList = networks!! // insert the fetched documents
                    updateNetworkList() // refresh the document list
                } else {
                    if (networks == null) {
                        CodingTools.displayToast(
                            mContext,
                            "Error fetching networks",
                            Toast.LENGTH_SHORT
                        )
                    } else { // got an empty array of networks so prompt user to create one
                        promptUserToCreateNetwork()
                    }
                }
            }
        })
    }

    /** This method will be initiated when the app realizes that the user does not have any
     * networks yet. A prompt will be shown to offer the user to create his first network.
     */
    private fun promptUserToCreateNetwork() {
        Log.d(TAG, "promptUserToCreateNetwork: ")
        val dialog = FirstNetworkDialog(mContext, object : ResultListener {
            override fun result(result: Boolean, message: String) {
                openNewNetworkDialog()
            }
        })
        CodingTools.openDialog(
            mContext,
            dialog,
            WindowManager.LayoutParams.WRAP_CONTENT,
            (mContext.resources.displayMetrics.widthPixels * 0.9).toInt(),
            0.9f
        )
    }

    /** This method will present the new network dialog, and will return with the result of the
     * creation
     */
    private fun openNewNetworkDialog() {
        Log.d(TAG, "createNewNetwork: ")
        val dialog = NewNetworkDialog(mContext, object : NetworkCallback {
            override fun getNetwork(network: SmartFarmNetwork) {
                Log.d(TAG, "getNetwork: $network")
                putNetworkToDb(network)
            }
        })
        CodingTools.openDialog(
            mContext,
            dialog,
            WindowManager.LayoutParams.WRAP_CONTENT,
            (mContext.resources.displayMetrics.widthPixels * 0.9).toInt(),
            0.9f
        )
    }

    /** This method will take the network object and will upload it to the cloud*/
    private fun putNetworkToDb(network: SmartFarmNetwork) {
        Log.d(TAG, "putNetworkToDb: ")
        dataController.insertDocument(
            MyAppClass.Constants.DB_NAME,
            MyAppClass.Constants.NETWORKS_COLLECTION,
            network,
            object : ResultListener {
                override fun result(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "result: successfully inserted document: $message")
                        network.id = message // retrieve the network ID
                        updateNetworkList()
                    } else {
                        Log.d(TAG, "result: ERROR: $message")
                        CodingTools.displayToast(mContext, message, Toast.LENGTH_SHORT)
                    }
                }
            })
    }

    /** This method will update the list of networks in the UI after adding a new one*/
    private fun updateNetworkList() {
        Log.d(TAG, "updateNetworkList: ")
        val adapter = NetworkListAdapter(
            requireContext(),
            networkList,
            object : NetworkCallback {
                override fun getNetwork(network: SmartFarmNetwork) {
                    openNetworkDetails(network)
                }
            }
        )
        networkRecycler.adapter = adapter
    }


    /** This method will open the selected network details dialog, that will display the devices*/
    private fun openNetworkDetails(network: SmartFarmNetwork) {
        Log.d(TAG, "openNetworkDetails: ")
        CodingTools.switchFragment(
            parentFragmentManager,
            R.id.main_LAY_mainFrame,
            NetworkDetailsFragment(mContext, network),
            true,
            "show_network_details"
        )
    }

    /** A method that initializes the views in the fragment*/
    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: Networks fragment")
        networkRecycler = mView.findViewById(R.id.networks_LST_networkList)
        createNetworkBtn = mView.findViewById(R.id.networks_BTN_addNetwork)
        createNetworkBtn.setOnClickListener {
            openNewNetworkDialog()
        }
    }
}