package com.example.smartfarm.models


/** A class that represents a smartfarm device
 * id - String, the device ID
 * name - String, the device chosen name
 * lastData - MeasuredData, the most recent measurement
 *
 *
 * Raw json:
 * {
 * "_id":{"$oid":"6138f67b9b0cc0e2903022fb"},
 * "did":"someId1" this is the physical device id as specified on the device
 * "name":"device name",
 * "active":"true/false value"
 * "description":"a string of the device details"
 * }
 */
class SmartFarmDevice {
    var did: String = ""
    var name: String = ""
    var active: Boolean = false
    var description: String = ""

    init {
    }

    override fun toString(): String {
        return "\nid: $did\nname: $name\nactive: $active\ndecription: $description\n"
    }
}