package com.crevitus.locationreminder.UI;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.provider.ReminderContentProvider;
import com.crevitus.locationreminder.utils.ReminderUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NewLocationDialogFragment extends DialogFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {
    private GoogleMap _map;
    private static final LatLngBounds UK_BOUNDS = new LatLngBounds(new LatLng(50.509992, 2.921206),
    new LatLng(61.161832, -11.009457));
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private boolean _locationLock = false;
    private GoogleApiClient _mapClient;
    private CircleOptions _circleAttributes;
    private Marker _curMarker;
    private Circle _curCircle;
    private String _curAddress;
    private LatLng _curLatLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_add_new_location, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Add Location");
        setHasOptionsMenu(true);

        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        //stop drawer being opened
        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //only get current location if fresh load
        if(savedInstanceState != null)
        {
            _locationLock = true;
        }

        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.radius);
        final TextView radLength = (TextView) rootView.findViewById(R.id.radiusLength);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int stepSize = 50;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //set seekbar step size
                progress = Math.round(progress / stepSize) * stepSize;
                seekBar.setProgress(progress);
                _circleAttributes.radius(progress);
                if (_curCircle != null) {
                    _curCircle.setRadius(progress);
                }
                radLength.setText("" + progress + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mAutocompleteView = (AutoCompleteTextView) rootView.findViewById(R.id.mapSearch);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlaceAutocompleteAdapter(getActivity(), R.layout.map_search_list_item, UK_BOUNDS, null);
        mAutocompleteView.setAdapter(mAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _mapClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        _circleAttributes = new CircleOptions()
                .fillColor(Color.argb(10, 0, 50, 240))
                .strokeColor(Color.argb(50, 0, 50, 240))
                .strokeWidth(2)
                .radius(50);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        if (_mapClient != null) {
            _mapClient.connect();
        }
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        _mapClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(getActivity() instanceof DialogCloseListener) {
            ((DialogCloseListener) getActivity()).handleDialogClose();
        }
    }

    //set map functions once loaded
    @Override
    public void onMapReady(GoogleMap map) {
        _map = map;
        _map.getUiSettings().setMyLocationButtonEnabled(true);
        _map.setMyLocationEnabled(true);
        _map.getUiSettings().setZoomControlsEnabled(true);

        _map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng)
                        .draggable(true);

                // Clears the previously touched position
                _map.clear();

                // Animating to the touched position
                _map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                _curMarker = _map.addMarker(markerOptions);

                _circleAttributes.center(latLng);

                _curCircle = _map.addCircle(_circleAttributes);

                _curLatLng = latLng;

                new GetAddressTask().execute(latLng);
            }
        });

        _map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (_curCircle != null) {
                    _curCircle.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                _curMarker = marker;
                new GetAddressTask().execute(marker.getPosition());
            }
        });
    }

    //query address from geocoder
    private class GetAddressTask extends AsyncTask<LatLng, Integer, String> {

        @Override
        protected String doInBackground(LatLng... latLng) {
            String address = "";
            Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
            try {

                List<Address> addresses = gcd.getFromLocation(latLng[0].latitude, latLng[0].longitude, 1);
                if (addresses.size() > 0)
                {
                    address = addresses.get(0).getAddressLine(0);
                }
            }
            catch (IOException i)
            {
               address = "Unknown Address";
            }
            return address;
        }

        protected void onPreExecute()
        {
            mAutocompleteView.setText("");
            mAutocompleteView.setHint("Searching for address");
        }

        protected void onPostExecute(String result) {
            _curAddress = result;
            _curMarker.setTitle(result);
            mAutocompleteView.setText(result);
            mAutocompleteView.setSelection(mAutocompleteView.getText().length());
            mAutocompleteView.clearFocus();
        }
    }

    //get auto complete predictions
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null :
                    getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

             //Retrieve the place ID of the selected item from the Adapter.
            final PlaceAutocompleteAdapter.PlaceAutocompleteItem item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.getPlaceId());

            //get additional info about place
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(_mapClient, placeId);
            placeResult.setResultCallback(_updatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> _updatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            LatLng location =  place.getLatLng();

            _map.clear();

            _curMarker =_map.addMarker(new MarkerOptions()
                    .position(location)
                    .draggable(true)
                    .title(place.getName().toString()));

            _curLatLng = location;
            _curAddress = place.getName().toString();
            _circleAttributes.center(location);
            _curCircle = _map.addCircle(_circleAttributes);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            location, 15);
            _map.animateCamera(cameraUpdate);

            places.release();
        }
    };


    @Override
    public void onLocationChanged(Location location) {
        if(!_locationLock) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 14);
            _map.animateCamera(cameraUpdate);
            _locationLock = !_locationLock;
        }
        else {
            LocationServices.FusedLocationApi.removeLocationUpdates(_mapClient, this);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mAdapter.setAPIClient(_mapClient);

        if(!_locationLock) {
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(_mapClient);
            if (location != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 14);
                _map.animateCamera(cameraUpdate);
            }

            LocationRequest locReq = LocationRequest.create();
            locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locReq.setInterval(5000); // Update location every 5 seconds

            LocationServices.FusedLocationApi.requestLocationUpdates(_mapClient, locReq,
                    this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mAdapter.setAPIClient(null);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mAdapter.setAPIClient(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.save_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if(_curMarker != null && _curAddress != null) {
                    ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(ReminderContentProvider.KEY_LAT, ""+ _curLatLng.latitude);
                    values.put(ReminderContentProvider.KEY_LNG, ""+ _curLatLng.longitude);
                    values.put(ReminderContentProvider.KEY_ADDRESS, _curAddress);
                    values.put(ReminderContentProvider.KEY_RADIUS, (int) _curCircle.getRadius());
                    Uri uri = contentResolver.insert(ReminderContentProvider.CONTENT_URI_LOCATIONS, values);
                    if(this.getArguments() != null)
                    {
                        values = new ContentValues();
                        values.put(ReminderContentProvider.KEY_REMINDER_TITLE, getArguments().getString("title"));
                        values.put(ReminderContentProvider.KEY_REMINDER_MESSAGE, getArguments().getString("message"));
                        values.put(ReminderContentProvider.KEY_DATETIME, getArguments().getLong("dateTime"));
                        values.put(ReminderContentProvider.KEY_REPETITION, getArguments().getLong("repetition"));
                        values.put(ReminderContentProvider.KEY_TYPE, getArguments().getString("type"));
                        values.put(ReminderContentProvider.KEY_LOCATION_ID, ContentUris.parseId(uri));
                        values.put(ReminderContentProvider.KEY_ENABLED, Reminder.STATE_ENABLED);
                        Uri rUri = contentResolver.insert(ReminderContentProvider.CONTENT_URI_REMINDERS, values);
                        ReminderUtils.addReminder(getActivity().getApplicationContext(), (int)ContentUris.parseId(rUri));
                        getFragmentManager().popBackStackImmediate();
                        getFragmentManager().popBackStackImmediate();
                    }
                    else {
                        getFragmentManager().popBackStackImmediate();
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Please add a location", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}