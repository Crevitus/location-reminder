package com.crevitus.locationreminder.model;

import com.google.android.gms.maps.model.LatLng;

//data model for reminder items
public class Reminder {
    private Integer _lID, _rID, _radius;
    private LatLng _latlng;
    private Long _startTime, _duration;
    private String _type;

    //public flags for state check
    public static final String STATE_ENABLED = "true";
    public static final String STATE_DISABLED = "false";

    public Reminder(Integer lID, Integer rID, LatLng latLng, Integer radius, String type, Long startTime) {
        _lID = lID;
        _rID = rID;
        _latlng = latLng;
        _radius = radius;
        _type = type;
        _startTime = startTime;
    }

    public int getLID()
    {
        return _lID;
    }

    public int getRID()
    {
        return _rID;
    }

    public LatLng getLatlng()
    {
        return _latlng;
    }

    public int getRadius()
    {
        return _radius;
    }

    public String getType()
    {
        return _type;
    }

    public long getStartTime()
    {
        return _startTime;
    }

}
