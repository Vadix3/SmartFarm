package com.example.smartfarm.models

import java.util.*

/** This is a model for the command object.
 * Command structure:
 * id (the command id)
 * deviceId (the target device id)
 * time (the time the command was issued)
 * type (command type - int)
 * data (string)
 * */

class CommandModel (){
    var id: String = UUID.randomUUID().toString()
    var deviceId: String = ""
    var time: Long = 0
    var type: Int = 0
    var data: String = ""

    init {
    }

    override fun toString(): String {
        return "\nid: $id\n" +
                "deviceId: $deviceId\n" +
                "time: $time\n" +
                "type: $type\n" +
                "data: $data"
    }
}