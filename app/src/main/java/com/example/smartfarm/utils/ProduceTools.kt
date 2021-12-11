package com.example.smartfarm.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.smartfarm.MyAppClass
import com.example.smartfarm.MyAppClass.Constants.DRY_WATER
import com.example.smartfarm.MyAppClass.Constants.FULL_OR_PARTIAL_SUN
import com.example.smartfarm.MyAppClass.Constants.FULL_SUN
import com.example.smartfarm.MyAppClass.Constants.MODERATE_MINUS_WATER
import com.example.smartfarm.MyAppClass.Constants.MODERATE_PLUS_WATER
import com.example.smartfarm.MyAppClass.Constants.MODERATE_WATER
import com.example.smartfarm.MyAppClass.Constants.PARTIAL_OR_SHADE_SUN
import com.example.smartfarm.MyAppClass.Constants.SHADE_OR_NO_SUN
import com.example.smartfarm.MyAppClass.Constants.SOAKED
import com.example.smartfarm.MyAppClass.Constants.TAG
import com.example.smartfarm.R
import com.example.smartfarm.models.CropDetails
import com.example.smartfarm.models.ProduceTip
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

object ProduceTools {

    /** soil moisture 0-700 +- (700 in water, 8 +- dry ground, 650 +- wet ground just watered
     * ,588,678 little drained after watering,
     * )
     * =========== DAY ============
     * light 0-1000+- (993-1010 direct sunlight,980- => small cloud, 970 cloud & shade
     * , 59 evening, 27-33 more evening)
     * uv 0-100 (40-100 direct sunlight,0-30 +- shade,10-20 clouds in shade, 15-35 clouds
     * ,12-13 clouds and cover & evening. 7 more evening)
     *  ######### Check UV sensor wiring #########
     *
     * - Water possible values (current measurement, very approximate):
     *      soaked = 650 +
     *      moderate =  550-600
     *      moderate+ = 600-650
     *      moderate- = 500-600
     *      dry soil = 500 -
     *
     * - UV possible values:
     *      full or partial = 20-100
     *      partial or shade = 10-50
     *      full = 50-100
     *      shade or no sun = 0-10
     *
     * - Light possible values:
     *      full or partial = 900-1000
     *      partial or shade = 800-950
     *      full = 980-1000
     *      shade or no sun = 0-100
     *
     * */


    /** This method will get the actual water value, and determine if the value
     * is one of the possible water buckets. for example:
     * the 'dry soil' bucket is for values between 0 and 500,
     * the method will put 'true' in the index location of the array, with respect to the
     * constant value, which is:
     * dry soil = 0
     * moderate- = 1
     * moderate = 2
     * moderate+ = 3
     * soaked = 4
     * for an input of '300', the returned array would be: (t,f,f,f,f)
     */
    private fun getWaterBucketsArray(actualWater: Int): MutableList<Boolean> {

        val waterFlagsBoolean = mutableListOf(false, false, false, false, false)

        if (actualWater < 500) { // dry soil
            waterFlagsBoolean[MyAppClass.Constants.DRY_WATER_FLAG] = true
        }

        if (actualWater in 501..549) { // moderate -
            waterFlagsBoolean[MyAppClass.Constants.MODERATE_MINUS_WATER_FLAG] = true
        }

        if (actualWater in 550..599) { // moderate
            waterFlagsBoolean[MyAppClass.Constants.MODERATE_WATER_FLAG] = true
        }

        if (actualWater in 600..650) { // moderate +
            waterFlagsBoolean[MyAppClass.Constants.MODERATE_PLUS_WATER_FLAG] = true
        }

        if (actualWater > 650) { // soaked
            waterFlagsBoolean[MyAppClass.Constants.SOAKED_FLAG] = true
        }

        return waterFlagsBoolean
    }

    private fun getRecommendedWaterRange(stringDescription: String): MutableList<Int> {

        if (stringDescription == DRY_WATER) { // dry soil
            return mutableListOf(0, 500)
        }

        if (stringDescription == MODERATE_MINUS_WATER) { // moderate -
            return mutableListOf(501, 549)
        }

        if (stringDescription == MODERATE_WATER) { // moderate
            return mutableListOf(550, 599)
        }

        if (stringDescription == MODERATE_PLUS_WATER) { // moderate +
            return mutableListOf(600, 650)
        }

        if (stringDescription == SOAKED) { // soaked
            return mutableListOf(650, 1000)
        }
        return mutableListOf(-1, 100)
    }


    /** This method will get the actual soil value, and the optimal value,
     * and will return an array of the following structure:
     * (actual value,recommended value,recommendation)
     * ("moderate-","moderate+","you should add water to the soil")
     */
    fun getWaterTips(
        mContext: Context,
        actualNumber: Int,
        optimalString: String
    ): ProduceTip {
        val tips = ProduceTip()
        tips.measuredValue = actualNumber.toString()
        tips.optimalString = optimalString
        val range = getRecommendedWaterRange(optimalString)
        tips.optimalValue = "(${range[0]} - ${range[1]})"
        // boolean array representation of the location of the actual number
        // in the various buckets (explained above the method) 
        // i.e (t,f,f,f,f)
        val booleanWaterBuckets = getWaterBucketsArray(actualNumber)

        val waterFlagsString = mutableListOf(
            MyAppClass.Constants.DRY_WATER,
            MyAppClass.Constants.MODERATE_MINUS_WATER,
            MyAppClass.Constants.MODERATE_WATER,
            MyAppClass.Constants.MODERATE_PLUS_WATER,
            MyAppClass.Constants.SOAKED
        )
        // the index of the recommended category
        val recommendedIndex = waterFlagsString.indexOf(optimalString)

        // the index of the real category
        val actualIndex = booleanWaterBuckets.indexOf(true)
        tips.measuredString = waterFlagsString[actualIndex]

        if (actualIndex > recommendedIndex) { // more water than needed
            tips.recommendation = mContext.getString(R.string.too_much_water)
            tips.alert = true
        }
        if (actualIndex < recommendedIndex) { // less water is needed
            tips.recommendation = mContext.getString(R.string.not_enough_water)
            tips.alert = true
        }
        if (actualIndex == recommendedIndex) { // water just right
            tips.recommendation = mContext.getString(R.string.water_just_right)
        }
        return tips
    }




    /** This method will fetch a single crop details object from the array using the name*/
    fun getSingleCropDetails(activity: Activity, name: String): CropDetails {
        val tempObject = CropDetails()
        val data = CodingTools.loadJSONFromAsset(activity, "crop_details.json")
        if (data != null) {
            val cropArray = data.get("crops") as JSONArray
            for (i in 0 until cropArray.length()) {
                val item = cropArray.get(i) as JSONObject
                if (item.get("name") == name) {
                    val tips = item.get("tips") as JSONObject
                    tempObject.id = item.get("id") as String
                    tempObject.name = item.get("name") as String
                    tempObject.sun = tips.get("sun") as String
                    tempObject.water = tips.get("water") as String
                    tempObject.minTemp = tips.get("min_temp") as Int
                    tempObject.maxTemp = tips.get("max_temp") as Int
                }
            }
        }
        return tempObject
    }

    /** This method will return a recommendation array for the temperature.
     * same as water
     */
    fun getTemperatureTips(
        mContext: Context,
        temperature: Double,
        minTemp: Int,
        maxTemp: Int
    ): ProduceTip {
        val tips = ProduceTip()
        tips.optimalValue = "$minTemp - $maxTemp°C"
        tips.measuredValue = temperature.toFloat().roundToInt().toString() + "°C"

        if (temperature < minTemp) { // too cold
            tips.recommendation = mContext.getString(R.string.too_cold)
            tips.alert = true
        }
        if (temperature > maxTemp) { // too hot
            tips.recommendation = mContext.getString(R.string.too_hot)
            tips.alert = true
        }
        return tips
    }

    /** This method will get the actual light value, and the optimal value,
     * and will return an array of the following structure:
     * (actual value,recommended value,recommendation)
     * ("shade or no sun","full sun","you should consider increasing sun exposure")
     */
    fun getLightTips(
        mContext: Context,
        actualNumber: Int,
        optimalString: String
    ): ProduceTip {
        val tips = ProduceTip()
        tips.optimalString = optimalString
        tips.measuredValue = actualNumber.toString()
        val range = getLightRecommendedRange(optimalString)
        tips.optimalValue = "(${range[0]} - ${range[1]})"

        // boolean array representation of the location of the actual number
        // in the various buckets (explained above the method)
        // i.e (t,f,f,f,f)
        val booleanLightBuckets = getLightBucketsArray(actualNumber)

        val lightFlagsString = mutableListOf(
            MyAppClass.Constants.SHADE_OR_NO_SUN,
            MyAppClass.Constants.PARTIAL_OR_SHADE_SUN,
            MyAppClass.Constants.FULL_OR_PARTIAL_SUN,
            MyAppClass.Constants.FULL_SUN
        )
        // the index of the recommended category
        val recommendedIndex = lightFlagsString.indexOf(optimalString)

        // the indexes of the real category locations
        val actualIndex = booleanLightBuckets.indexOf(true)
        if (actualIndex == -1) { // real value is not in any bucket
            Log.d(TAG, "getLightTips: No recommendations")
        } else {
            // Add the actual categories to the tips
            tips.measuredString = lightFlagsString[actualIndex]

            if (actualIndex > recommendedIndex) { // too much light
                tips.recommendation = mContext.getString(R.string.too_much_light)
                tips.alert = true
            }
            if (actualIndex < recommendedIndex) { // not enough light
                tips.recommendation = mContext.getString(R.string.not_enough_light)
                tips.alert = true
            }
            if (actualIndex == recommendedIndex) { // good light
                tips.recommendation = mContext.getString(R.string.light_just_right)
            }
        }
        return tips
    }

    /** This method does the same as the water buckets method, but for light
     * and with multiple light categories possible.
     * - Light possible values:
     *   shade or no sun = 0-100
     *   partial or shade = 800-950
     *   full or partial = 900-1000
     *   full = 980-1000
     *
     *   (shade or no sun, partial or shade, full or partial, full sun)
     */
    private fun getLightBucketsArray(actualLight: Int): MutableList<Boolean> {

        val lightFlagsBoolean = mutableListOf(false, false, false, false)

        if (actualLight < 100) { // shade or no sun
            lightFlagsBoolean[MyAppClass.Constants.SHADE_OR_NO_SUN_FLAG] = true
        }

        if (actualLight in 800..899) { // partial or shade
            lightFlagsBoolean[MyAppClass.Constants.PARTIAL_OR_SHADE_SUN_FLAG] = true
        }

        if (actualLight in 900..979) { // full or partial
            lightFlagsBoolean[MyAppClass.Constants.FULL_OR_PARTIAL_SUN_FLAG] = true
        }

        if (actualLight in 980..1000) { // full sun
            lightFlagsBoolean[MyAppClass.Constants.FULL_SUN_FLAG] = true
        }

        return lightFlagsBoolean
    }

    private fun getLightRecommendedRange(actualLight: String): MutableList<Int> {
        Log.d(TAG, "getLightRecommendedRange: $actualLight")
        if (actualLight == SHADE_OR_NO_SUN) { // shade or no sun
            return mutableListOf(0, 100)
        }

        if (actualLight == PARTIAL_OR_SHADE_SUN) {
            return mutableListOf(800, 899)
        }

        if (actualLight == FULL_OR_PARTIAL_SUN) {
            return mutableListOf(900, 979)
        }

        if (actualLight == FULL_SUN) {
            return mutableListOf(980, 1500)
        }

        return mutableListOf(-1, 1500)
    }


    private fun getUVRecommendedRange(actualLight: String): MutableList<Int> {
        Log.d(TAG, "getUVRecommendedRange: $actualLight")
        if (actualLight == SHADE_OR_NO_SUN) { // shade or no sun
            return mutableListOf(0, 11)
        }

        if (actualLight == PARTIAL_OR_SHADE_SUN) {
            return mutableListOf(12, 30)
        }

        if (actualLight == FULL_OR_PARTIAL_SUN) {
            return mutableListOf(31, 50)
        }

        if (actualLight == FULL_SUN) {
            return mutableListOf(51, 100)
        }

        return mutableListOf(-1, 1500)
    }

    fun getUVTips(
        mContext: Context,
        actualNumber: Int,
        optimalString: String
    ): ProduceTip {
        val tips = ProduceTip()
        tips.optimalString = optimalString
        tips.measuredValue = actualNumber.toString()
        val range = getUVRecommendedRange(optimalString)
        tips.optimalValue = "(${range[0]} - ${range[1]})"
        // boolean array representation of the location of the actual number
        // in the various buckets (explained above the method)
        // i.e (t,f,f,f,f)
        val booleanUVBuckets = getUVBucketsArray(actualNumber)

        val uvFlagsString = mutableListOf(
            MyAppClass.Constants.SHADE_OR_NO_SUN,
            MyAppClass.Constants.PARTIAL_OR_SHADE_SUN,
            MyAppClass.Constants.FULL_OR_PARTIAL_SUN,
            MyAppClass.Constants.FULL_SUN
        )
        // the index of the recommended category
        val recommendedIndex = uvFlagsString.indexOf(optimalString)

        // the indexes of the real category locations
        val actualIndex = booleanUVBuckets.indexOf(true)
        if (actualIndex == -1) { // real value is not in any bucket
            Log.d(TAG, "getUVTips: No recommendations")
        } else {
            // Add the actual categories to the tips
            tips.measuredString = uvFlagsString[actualIndex]

            if (actualIndex > recommendedIndex) { // too much sun
                tips.recommendation = mContext.getString(R.string.too_much_sun)
                tips.alert = true
            }
            if (actualIndex < recommendedIndex) { // not enough sun
                tips.recommendation = mContext.getString(R.string.not_enough_sun)
                tips.alert = true
            }
            if (actualIndex == recommendedIndex) { // good sun
                tips.recommendation = mContext.getString(R.string.light_just_sun)
            }
        }
        return tips
    }

    /**
     *      * - UV possible values:
     *      shade or no sun = 0-10
     *      partial or shade = 10-50
     *      full or partial = 20-100
     *      full = 50-100
     */
    private fun getUVBucketsArray(actualUV: Int): MutableList<Boolean> {

        val uvFlagsBoolean = mutableListOf(false, false, false, false)

        if (actualUV < 11) { // shade or no sun
            uvFlagsBoolean[MyAppClass.Constants.SHADE_OR_NO_SUN_FLAG] = true
        }

        if (actualUV in 12..30) { // partial or shade
            uvFlagsBoolean[MyAppClass.Constants.PARTIAL_OR_SHADE_SUN_FLAG] = true
        }

        if (actualUV in 31..50) { // full or partial
            uvFlagsBoolean[MyAppClass.Constants.FULL_OR_PARTIAL_SUN_FLAG] = true
        }

        if (actualUV in 51..100) { // full sun
            uvFlagsBoolean[MyAppClass.Constants.FULL_SUN_FLAG] = true
        }

        return uvFlagsBoolean
    }
}