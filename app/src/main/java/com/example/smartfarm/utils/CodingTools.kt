package com.example.smartfarm.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.smartfarm.R

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

}