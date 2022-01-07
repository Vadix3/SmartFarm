package com.example.smartfarm.models

/** This class represents the data for the graphs and charts.
 * date = the date the data was measured
 * time = the time the data was measured
 * data = the data value itself
 */
class MicroData(date: String, time: String, data: String) {

    var date = date
    var time = time
    var data = data

    init {
    }

    override fun toString(): String {
        return "\ndate: $date\n" +
                "time: $time\n" +
                "data: $data\n"
    }
}