package com.example.smartfarm.models

/** A class that represents a farm network instance
 * id - String, the ID of the network
 * name - String, the network name
 * icon - String, the network icon
 * devices - ArrayList, an arraylist of the network devices
 *
 *  * Raw json:
 * {
 * "_id":{"$oid":"6138f67b9b0cc0e2903022fb"},
 * "name":"08/09/21",
 * "icon":"url",
 * "owner":"ownerEmail"
 * "devices":[1,2,3,4,5] array of device id's
 * }
 */

class SmartFarmNetwork {
    var id: String = ""
    var name: String = ""
    var owner:String = ""
    var devices = arrayListOf<String>()

    init {
    }

    override fun toString(): String {
        return "\nid: $id\nname: $name\nowner: $owner\ndevices: $devices\n"
    }
}