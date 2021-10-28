package com.example.smartfarm.models

/** A class that represents the produce row of the drop down list
 * */
class ProduceRow {
    var icon: Int = 0 // the resource id
    var name: String = "" // the crop name

    init {
    }

    override fun toString(): String {
        return "\nicon: $icon\nname: $name"
    }
}