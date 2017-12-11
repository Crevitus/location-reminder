package com.crevitus.locationreminder.model;


//data model for list items displaying location
public class LocationListItem {
    private int _id;
    private String _lat, _lng, _address;
    private int _radius;

    public LocationListItem(int id, String lat, String lng, int radius, String address)
    {
        _id = id;
        _lat = lat;
        _lng = lng;
        _radius =  radius;
        _address = address;
    }

    public int getID() {
        return _id;
    }

    public String getLat() {
        return _lat;
    }

    public String getLng() {
        return _lng;
    }

    public int getRadius() {
        return _radius;
    }

    public String getAddress() {
        return _address;
    }
}
