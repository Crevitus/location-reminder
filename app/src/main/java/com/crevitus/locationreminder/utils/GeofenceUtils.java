package com.crevitus.locationreminder.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.receiver.GeofenceTransitionsReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

public class GeofenceUtils implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Context _context;
    private GoogleApiClient _APIClient;
    private boolean _add;
    private int _id;
    private Geofence _geofence;
    private static final int GEOFENCE_DEFAULT_RADIUS = 30;
    public static final String GEOFENCE_FLAG_ENTERING = "arriving";
    public static final String GEOFENCE_FLAG_EXITING = "leaving";

    public GeofenceUtils(Context context)
    {
        _context = context;
        _APIClient = new GoogleApiClient.Builder(_context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void addGeoFence(Reminder reminder)
    {
        _id  = reminder.getRID();
        int radius;
        int transitionType;
        if(reminder.getRadius() == 0)
        {
            radius = GEOFENCE_DEFAULT_RADIUS;
        }
        else
        {
            radius = reminder.getRadius();
        }
        if(reminder.getType().equals(GEOFENCE_FLAG_ENTERING))
        {
            transitionType = Geofence.GEOFENCE_TRANSITION_ENTER;
        }
        else
        {
            transitionType = Geofence.GEOFENCE_TRANSITION_EXIT;
        }
             _geofence = new Geofence.Builder()
                .setRequestId("" + reminder.getRID())
                .setCircularRegion(reminder.getLatlng().latitude,
                        reminder.getLatlng().longitude,
                        radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(transitionType)
                .build();
        _add = true;
        if(!_APIClient.isConnected()) {
            _APIClient.connect();
        }
        else
        {
            setGeofence();
        }
    }

    private void setGeofence()
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.addGeofence(_geofence);

        GeofencingRequest request = builder.build();

        Intent intent = new Intent(_context, GeofenceTransitionsReceiver.class);
        intent.putExtra("id", _id);

        LocationServices.GeofencingApi.addGeofences(
                _APIClient,
                request,
                PendingIntent.getBroadcast(_context, _id, intent, 0)
        );
    }

    public void removeGeoFence(int id)
    {
        _id = id;
        _add = false;
        if(!_APIClient.isConnected()) {
            _APIClient.connect();
        }
        else
        {
            removeGeofence();
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        if(_add) {
            setGeofence();
        }

        else {
            removeGeofence();
        }
    }

    private void removeGeofence()
    {
        Intent intent = new Intent(_context, GeofenceTransitionsReceiver.class);
        intent.putExtra("id", _id);
        LocationServices.GeofencingApi.removeGeofences(
                _APIClient,
                // This is the same pending intent that was used in addGeofences().
                PendingIntent.getBroadcast(_context, _id, intent, 0));
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
