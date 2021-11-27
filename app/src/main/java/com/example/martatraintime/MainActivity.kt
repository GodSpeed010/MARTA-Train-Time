package com.example.martatraintime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.martatraintime.models.Train
import okhttp3.Headers
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
                Log.d("my_tag", "Retreived data for ${trains.size} trains")

                //TODO
                setStationSpinner(trains)

                findViewById<Button>(R.id.bt_get).setOnClickListener {
                    val station: String = findViewById<Spinner>(R.id.sp_stations).selectedItem.toString()
                    val direction: String = findViewById<Spinner>(R.id.sp_directions).selectedItem.toString()
                    val line: String = findViewById<Spinner>(R.id.sp_rail_lines).selectedItem.toString()

                    //create Train object using user inputted data
                    val userTrain: Train = Train(station, direction, line)

                    Log.d("my_tag", "user station is $station, direction is $direction, and line is $line")

                    var found: Boolean = false
                    for (i in 0 until trains.size) {

                        if (trains[i].equals(userTrain)) {
                            found = true
                            Log.d("my_tag", "FOUND A MATCH")
                            findViewById<TextView>(R.id.tv_wait_time).text = trains[i].waitingTime
                        }

                    }
                    if (!found) {
                        Toast.makeText(applicationContext,
                        "Not Found",
                        Toast.LENGTH_SHORT).show()

                        findViewById<TextView>(R.id.tv_wait_time).text = ""
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
}