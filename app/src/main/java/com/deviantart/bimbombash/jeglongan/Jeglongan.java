package com.deviantart.bimbombash.jeglongan;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by bob on 02/01/16.
 */

public class Jeglongan{
    private Marker marker;
    private int counter;
    private String ID;
    private LatLng latLng;

    public Marker getMarker() {
        return marker;
    }

    public int getCounter() {
        return counter;
    }

    public String getID() {
        return ID;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public List<String> getBerita() {
        return berita;
    }

    private List<String> berita;

    Jeglongan(String ID, Marker marker, int counter) {
        this.ID = ID;
        this.marker = marker;
        this.counter = counter;
        latLng = marker.getPosition();
    }


}