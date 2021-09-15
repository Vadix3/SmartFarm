package com.example.smartfarm.interfaces

/** An interface that will return the result of an async operation
 *  result - Boolean variable that will return true in case of a successful fetch
 *  message - A string representation of the response.
 * */

interface ResultListener {
    fun result(result: Boolean, message: String)
}