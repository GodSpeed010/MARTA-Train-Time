package com.example.martatraintime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.martatraintime.models.Train
import okhttp3.Headers
import okhttp3.internal.wait
import java.util.*
import kotlin.collections.HashSet

class MainActivity : AppCompatActivity() {

    val martaEndpoint: String = "http://developer.itsmarta.com/RealtimeTrain/RestServiceNextTrain/GetRealtimeArrivals"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client: AsyncHttpClient = AsyncHttpClient()

        client[martaEndpoint, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.d("my_tag", "JSON success")

                val trains: List<Train> = Train.fromJsonArray(json.jsonArray)
                Log.d("my_tag", "Retrieved data for ${trains.size} trains")

                setStationSpinner(trains)

                findViewById<Button>(R.id.bt_get).setOnClickListener {
                    val station: String = findViewById<Spinner>(R.id.sp_stations).selectedItem.toString()
                    val direction: String = findViewById<Spinner>(R.id.sp_directions).selectedItem.toString()
                    val line: String = findViewById<Spinner>(R.id.sp_rail_lines).selectedItem.toString()

                    //create Train object using user inputted data
                    val userTrain: Train = Train(station, direction, line)

                    Log.d("my_tag", "user station is $station, direction is $direction, and line is $line")

                    var found: Boolean = false
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
        val stationList = stationNames.toMutableList()

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item, stationList
        )

        findViewById<Spinner>(R.id.sp_stations).adapter = adapter

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
}