package com.example.smartfarm.models


/** A class representation of a ProduceTip object where:
 * var measuredValue: String = the real value measured by the sensors. Example: 234
 * var optimalValue: String = the optimal value
 * var measuredString: String = the real value measured, string category
 * var optimalString: String = the optimal value measured, string category. Example: Full or partial sun
 * var recommendation: String = phrase recommendation. Example: 'You should consider adding water'
 */
class ProduceTip {

    var name: String = ""
    var measuredValue: String = ""
    var optimalValue: String = ""
    var measuredString: String = ""
    var optimalString: String = ""
    var recommendation: String = ""
    var alert: Boolean = false


    init {
    }

    override fun toString(): String {
        return "\nmeasuredValue: $measuredValue\n" +
                "optimalValue: $optimalValue\n" +
                "measuredString: $measuredString\n" +
                "optimalString: $optimalString\n" +
                "recommendation: $recommendation\n"
    }
}