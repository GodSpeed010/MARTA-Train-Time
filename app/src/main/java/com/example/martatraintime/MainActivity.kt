package com.example.martatraintime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.martatraintime.models.Train
import com.google.android.material.snackbar.Snackbar
import okhttp3.Headers
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    val martaEndpoint: String = "http://developer.itsmarta.com/RealtimeTrain/RestServiceNextTrain/GetRealtimeArrivals"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO set the spinner for stations
//        setStationSpinner()
        //temp solution, just compare strings

        val client: AsyncHttpClient = AsyncHttpClient()

        client[martaEndpoint, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.d("my_tag", "JSON success")

                val trains: List<Train> = Train.fromJsonArray(json.jsonArray)
                Log.d("my_tag", "Retreived data for ${trains.size} trains")

                findViewById<Button>(R.id.bt_get).setOnClickListener {
                    val station: String = findViewById<EditText>(R.id.et_station).text.toString()
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

//                    //loop through every json object
//                    for(i in 0..json.jsonArray.length()) {
//                        //if direction, station(ignorecase), and line match
//
//
//                        if("example".equals("EXAMPLE", ignoreCase = true))
//                        Log.d("my_tag", "data at index $i is ${json.jsonArray.getJSONObject(i)}")
//                    }

                }

            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String, throwable: Throwable?) {
                Log.d("my_tag", "failure")
            }
        }]


    }

    //TODO
//    fun setStationSpinner() {
//        val cardinalDirections = resources.getStringArray(R.array.cardinal_directions)
//        val spinner = findViewById<Spinner>(R.id.sp_rail_lines)
//        val adapter = ArrayAdapter(this,
//            android.R.layout.simple_spinner_item, cardinalDirections)
//        spinner.adapter = adapter
//    }
}