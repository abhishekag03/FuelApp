package com.example.android.fuelapp.data;

/**
 * Created by vishaal on 11/7/17.
 */
public class MapObject {
    public double distance;
    public double latitude;
    public double longitude;

    public MapObject(double d, double lat, double lon) {
        this.distance = d;
        this.latitude = lat;
        this.longitude = lon;
    }
}