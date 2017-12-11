package com.crevitus.locationreminder.UI;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.adapter.LocationListAdapter;
import com.crevitus.locationreminder.model.LocationListItem;
import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.provider.ReminderContentProvider;
import com.crevitus.locationreminder.utils.ReminderUtils;

import java.util.ArrayList;
import java.util.List;

public class LocationListDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks {
    View _rootView;
    private List<LocationListItem> _listData;
    private LocationListAdapter _listAdapter;
    private LocationListItem _selectedLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.dialog_locations_list, container, false);

        //set actionbar settings
        Toolbar toolbar = (Toolbar) _rootView.findViewById(R.id.toolbar);
        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Select Location");
        setHasOptionsMenu(true);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        //stop drawer from opening
        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //initialise data array
        _listData = new ArrayList<LocationListItem>();

        // get the listview
        ListView listView = (ListView) _rootView.findViewById(R.id.lstLocations);

        //onclick location click display selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final LocationListItem item = (LocationListItem) _listAdapter.getItem(position);
                TextView selectedLocations = (TextView) _rootView.findViewById(R.id.txtSelectedLocation);
                selectedLocations.setVisibility(View.VISIBLE);
                 Spanned text = Html.fromHtml("<b>Selected:</b> " + item.getAddress());
                selectedLocations.setText(text);
                _selectedLocation = item;
            }
        });

        //get adapter
        _listAdapter = new LocationListAdapter(getActivity(), _listData);

        // set list adapter
        listView.setAdapter(_listAdapter);

        //get data from content provider
        getLoaderManager().initLoader(0, null, this);

        return _rootView;
    }

    //reload data on resume
    @Override
    public void onResume() {
        super.onResume();
        _listData.clear();
        getLoaderManager().restartLoader(0, null, this);
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
                //check whether location has been selected
                if (_selectedLocation != null) {
                        //right reminder info to database
                        ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
                        ContentValues values = new ContentValues();
                        values.put(ReminderContentProvider.KEY_REMINDER_TITLE, getArguments().getString("title"));
                        values.put(ReminderContentProvider.KEY_REMINDER_MESSAGE, getArguments().getString("message"));
                        values.put(ReminderContentProvider.KEY_DATETIME, getArguments().getLong("dateTime"));
                        values.put(ReminderContentProvider.KEY_REPETITION, getArguments().getLong("repetition"));
                        values.put(ReminderContentProvider.KEY_TYPE, getArguments().getString("type"));
                        values.put(ReminderContentProvider.KEY_LOCATION_ID, _selectedLocation.getID());
                        values.put(ReminderContentProvider.KEY_ENABLED, Reminder.STATE_ENABLED);
                        Uri rUri = contentResolver.insert(ReminderContentProvider.CONTENT_URI_REMINDERS, values);
                        ReminderUtils.addReminder(getActivity().getApplicationContext(), (int) ContentUris.parseId(rUri));
                        //return to main activity
                        getFragmentManager().popBackStackImmediate();
                        getFragmentManager().popBackStackImmediate();
                }
                else {
                    Toast.makeText(getActivity(), "Please add a location", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //get all locations
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader loader = new android.support.v4.content.CursorLoader(getActivity(),
                ReminderContentProvider.CONTENT_URI_LOCATIONS,
                null,
                null,
                null,
                null);
        return loader;
    }

    //display locations in list
    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        while (cursor.moveToNext()) {
            _listData.add(new LocationListItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4)));
        }
        cursor.close();
        _listAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }
}
