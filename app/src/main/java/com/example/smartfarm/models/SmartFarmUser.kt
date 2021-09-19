package com.example.smartfarm.models


/** A class that represents an app user
 *
 * name - String, the name of the user
 * email - String, the users email
 * password - String, the users password
 *
 *  * Raw json:
 * {
 * "name":"user name",
 * "email":"user email"
 * "password": "pass"
 * }
 */
class SmartFarmUser {
    var name: String = ""
    var email: String = ""
    var password:String = ""
    init {
    }

    override fun toString(): String {
        return "\nname: $name\nlast email: $email\npassword: $password"
    }
}