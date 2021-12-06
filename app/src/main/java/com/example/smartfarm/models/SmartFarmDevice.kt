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
 * "produce":"string, the name of the crop that the device measures"
 * "description":"a string of the device details"
 * "measure_interval":"a double value representing the measure interval in hours"
 * }
 */
class SmartFarmDevice {
    var did: String = ""
    var name: String = ""
    var active: Boolean = false
    var produce: String = ""
    var description: String = ""
    var measure_interval:Double = 0.0

    init {
    }

    override fun toString(): String {
        return "\ndid: $did\nname: $name\nactive: $active\nproduce: $produce\n" +
                "decription: $description\n" +
                "interval: $measure_interval\n"
    }
}