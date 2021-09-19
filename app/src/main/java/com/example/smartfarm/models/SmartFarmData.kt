package com.example.smartfarm.models


/** A class that represents the measured data from a given device
 * device - String, the ID of the device that measured the data
 * date - String, the date of the measurement
 * time - String, the time of the measurement
 * humidity - Double
 * temperature - Double
 * soil - Int, the measured soil moisture
 * light - Int, the measured light exposure
 * uv - Int, the measured uv exposure
 * */

class SmartFarmData() {
    var device: String = ""
    var date: String = ""
    var time: String = ""
    var humidity: Double = 0.0
    var temperature: Double = 0.0
    var soil: Int = 0
    var light: Int = 0
    var uv: Int = 0

    init {
    }

    override fun toString(): String {
        return "\ndevice: $device\ndate: $date\ntime: $time\nhumidity: $humidity\ntemperature: $temperature\n"+
                "soil: $soil\nlight: $light\nuv: $uv"
    }
}
