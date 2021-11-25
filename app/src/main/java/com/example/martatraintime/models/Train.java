package com.example.martatraintime.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Train {
    String station;
    String direction;
    String line;
    String waitingTime;

    Train(JSONObject jsonObject) throws JSONException {
        this.station = jsonObject.getString("STATION");
        this.direction = jsonObject.getString("DIRECTION");
        this.line = jsonObject.getString("LINE");
        this.waitingTime = jsonObject.getString("WAITING_TIME");
    }

    public Train(String station, String direction, String line) {
        this.station = station;
        this.direction = direction;
        this.line = line;
    }

    //creates List of Trains from JSONArray
    public static List<Train> fromJsonArray(JSONArray trainJsonArray) throws JSONException {
        List<Train> trains = new ArrayList<>();

        for (int i = 0; i < trainJsonArray.length(); i++) {
            trains.add(new Train(trainJsonArray.getJSONObject(i)));
        }

        return trains;
    }

    public String getStation() {
        return station;
    }

    public String getDirection() {
        return direction;
    }

    public String getLine() {
        return line;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public Boolean equals(Train train) {
        return this.station.equalsIgnoreCase(train.station) && this.direction.equalsIgnoreCase(train.direction) && this.line.equalsIgnoreCase(train.line);
    }
}
