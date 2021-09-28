package com.example.smartfarm.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.R
import com.example.smartfarm.dialogs.NewNetworkDialog
import com.example.smartfarm.interfaces.NetworkCallback
import com.example.smartfarm.models.SmartFarmNetwork

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

}