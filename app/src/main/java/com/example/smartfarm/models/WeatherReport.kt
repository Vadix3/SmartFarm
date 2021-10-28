package com.example.smartfarm.models

/** This is a class representation for a weather report with the selected fields:
 * date,sunrise,sunset,temp,feels_like,humidity,wind_speed,weather_description, icon
 */
class WeatherReport() {
    var date: Long = 0
    var sunrise: Long = 0
    var sunset: Long = 0
    var temp: Double = 0.0
    var feels_like: Double = 0.0
    var humidity: Int = 0
    var wind_speed: Double = 0.0
    var weather_description: String = ""
    var icon: String = ""

    init {
    }

    override fun toString(): String {
        return "\ndate: $date\nsunrise: $sunrise\nsunset: $sunset\ntemp: $temp\nfeels_like: $feels_like\n" +
                "humidity: $humidity\nwind_speed: $wind_speed\ndescription: $weather_description\nicon: $icon\n"
    }
}