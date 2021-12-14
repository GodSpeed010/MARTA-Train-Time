package com.example.martatraintime

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.martatraintime.models.Train
import okhttp3.Headers
import java.util.*
import android.widget.AutoCompleteTextView

import android.widget.ArrayAdapter




class MainActivity : AppCompatActivity() {

    val martaEndpoint: String = "http://developer.itsmarta.com/RealtimeTrain/RestServiceNextTrain/GetRealtimeArrivals"

    val WAIT_TIME_KEY = "wait_time_key"
    val STATION_NAME_KEY = "station_name_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setStaticDropdowns()

        val client: AsyncHttpClient = AsyncHttpClient()

        client[martaEndpoint, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.d("my_tag", "JSON success")

                val trains: List<Train> = Train.fromJsonArray(json.jsonArray)
                Log.d("my_tag", "Retrieved data for ${trains.size} trains")

                setStationSpinner(trains)

                restoreViewsFromInstance(savedInstanceState)

                findViewById<Button>(R.id.bt_get).setOnClickListener {
                    val station: String = findViewById<AutoCompleteTextView>(R.id.sp_stations).text.toString()
                    val direction: String = findViewById<AutoCompleteTextView>(R.id.sp_directions).text.toString()
                    val line: String = findViewById<AutoCompleteTextView>(R.id.sp_rail_lines).text.toString()

                    //create Train object using user inputted data
                    val userTrain: Train = Train(station, direction, line)

                    Log.d("my_tag", "user station is $station, direction is $direction, and line is $line")

                    var minTimeIndex: Int = -1
                    for (i in trains.indices) {

                        if (trains[i].equals(userTrain)) {
                            Log.d("my_tag", "MATCH FOUND at index $i")

                            //if this is first match, set this index as minTimeIndex
                            if (minTimeIndex == -1) {
                                Log.d("my_tag", "FIRST MATCH")
                                Log.d("my_tag", "train wait_time is ${trains[i].waitingTime}")
                                minTimeIndex = i
                            }

                            //wait time can be 'Arriving', so check if waitTime has an Int
                            if ( !hasInt(trains[i].waitingTime) ) {
                                Log.d("my_tag", "waitingTime has no Int")
                                //set minTimeIndex and break out of loop because 'Arriving' is the soonest possibility
                                minTimeIndex = i
                                break
                            } else {
                                val currWaitTime: Int = trains[i].waitingTime.filter { it.isDigit() }.toInt()
                                val minWaitTime: Int = trains[minTimeIndex].waitingTime.filter { it.isDigit() }.toInt()

                                Log.d("my_tag", "checking if $currWaitTime < $minWaitTime")
                                //if current iteration's wait time is less than trains[minTimeIndex]'s, adjust minTimeIndex
                                if (currWaitTime < minWaitTime) {
                                    Log.d("my_tag", "condition True; changed minTimeIndex to $i")
                                    minTimeIndex = i
                                }
                            }

                        }

                    }

                    //if minTimeIndex has default val of -1, there was no match
                    if (minTimeIndex == -1) {
                        Log.d("my_tag", "No Match")

                        Toast.makeText(applicationContext,
                        "Not Found",
                        Toast.LENGTH_SHORT).show()

                        findViewById<TextView>(R.id.tv_wait_time).text = ""
                    } else {
                        Log.d("my_tag", "displaying ${trains[minTimeIndex].waitingTime}")
                        findViewById<TextView>(R.id.tv_wait_time).text = trains[minTimeIndex].waitingTime
                    }

                }

            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String, throwable: Throwable?) {
                Log.d("my_tag", "failure")
                findViewById<AutoCompleteTextView>(R.id.sp_stations).setText("Internet Error $statusCode", false)
            }
        }]

    }

    //populate the sp_stations Spinner with the list of station names retrieved from the APi
    fun setStationSpinner(trains: List<Train>) {
        //station names will be ordered in alphabetical order with no duplicates
        val stationNames: TreeSet<String> = TreeSet()

        //populate stationNames with the list of all station names retrieved from API
        for (train in trains) {
            stationNames.add(train.station)
        }

        //convert stationNames to MutableList so it can be used with adapter
        val stationArray = stationNames.toTypedArray()

        setDropdown(stationArray, R.id.sp_stations)
    }

    fun setStaticDropdowns() {
        setCustomDropdown(
            resources.getStringArray(R.array.rail_lines),
            R.id.sp_rail_lines
        )

        setCustomDropdown(
            resources.getStringArray(R.array.cardinal_directions),
            R.id.sp_directions
        )
    }

    fun setCustomDropdown(stringArray: Array<String>, dropdownId: Int) {
        val adapter: MyAdapter = MyAdapter(
            this,
            stringArray
        )

        val editTextFilledExposedDropdown = findViewById<AutoCompleteTextView>(dropdownId)
        editTextFilledExposedDropdown.setAdapter(adapter)

        //auto-populate the dropdown with the first value in array so no need to handle for empty String
        editTextFilledExposedDropdown.setText(editTextFilledExposedDropdown.adapter.getItem(0).toString(), false)
    }

    fun setDropdown(stringArray: Array<String>, dropdownId: Int) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            stringArray
        )

        val editTextFilledExposedDropdown = findViewById<AutoCompleteTextView>(dropdownId)
        editTextFilledExposedDropdown.setAdapter(adapter)

        //auto-populate the dropdown with the first value in array so no need to handle for empty String
        editTextFilledExposedDropdown.setText(editTextFilledExposedDropdown.adapter.getItem(0).toString(), false)
    }

    fun hasInt(s: String): Boolean {
        //loop through String
        for (x in s) {
            //if any digits are found, return true
            if(x.isDigit()) {
                return true
            }
        }
        //else no digits were found
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //get the wait-time and station-name from the views
        val waitTime: String = findViewById<TextView>(R.id.tv_wait_time).text.toString()
        val stationName: String = findViewById<AutoCompleteTextView>(R.id.sp_stations).text.toString()

        Log.d("my_tag", "onSaveInstanceState: waitTime is $waitTime")
        Log.d("my_tag", "onSaveInstanceState: stationName is $stationName")

        outState.putString(WAIT_TIME_KEY, waitTime)
        outState.putString(STATION_NAME_KEY, stationName)
    }

    //restore views from savedInstanceState. Not using onRestoreInstanceState because Spinners are being populated in onCreate, so it would be overwritten
    fun restoreViewsFromInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            Log.d("my_tag", "onRestoreInstanceState: waitTime is ${savedInstanceState.getString(WAIT_TIME_KEY)}")
            Log.d("my_tag", "onRestoreInstanceState: stationName is ${savedInstanceState.getString(STATION_NAME_KEY)}")

            findViewById<TextView>(R.id.tv_wait_time).text = savedInstanceState.getString(WAIT_TIME_KEY)
            findViewById<AutoCompleteTextView>(R.id.sp_stations).setText(savedInstanceState.getString(STATION_NAME_KEY), false)
        }
    }
}

//custom Adapter to prevent bug in dropdowns when switching orientation
class MyAdapter(context: Context, val items: Array<String>)
    : ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, items) {

    private val noOpFilter = object : Filter() {
        private val noOpResult = FilterResults()
        override fun performFiltering(constraint: CharSequence?) = noOpResult
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
    }

    override fun getFilter() = noOpFilter
}