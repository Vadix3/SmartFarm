package com.example.smartfarm.models

/** A class that represents the crop details.
 * Consists of:
 * id - Int
 * name - String, the crop name
 * sun - String, the sun amount required
 * water - String, the water amount required
 * minTemp - Int, minimum temperature (Celsius)
 * maxTemp - Intm, maximum temperature (Celsius)
 *
 *
 * ----------------------
 *
 * Sun:
 * - Full
 * - Full or partial
 * - Partial or shade
 * - Shade or no sun
 * - Partial
 * Water:
 * - Moderate+
 * - Moderate
 * - Moderate-
 *
 */
class CropDetails {

    var id: String = ""
    var name: String = ""
    var sun: String = ""
    var water: String = ""
    var minTemp: Int = 0
    var maxTemp: Int = 0


    init {
    }

    override fun toString(): String {
        return "\nid: $id\n" +
                "name: $name\n" +
                "sun: $sun\n" +
                "water: $water\n" +
                "min temp: $minTemp\n" +
                "max temp: $maxTemp"
    }
}