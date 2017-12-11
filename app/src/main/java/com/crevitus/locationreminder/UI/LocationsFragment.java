package com.crevitus.locationreminder.UI;

import android.content.ContentUris;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.adapter.LocationListAdapter;
import com.crevitus.locationreminder.model.LocationListItem;
import com.crevitus.locationreminder.provider.ReminderContentProvider;

import java.util.ArrayList;
import java.util.List;

import de.timroes.android.listview.EnhancedListView;


public class LocationsFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks {
    View _rootView;
    private List<LocationListItem> _listData;
    private LocationListAdapter _listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.fragment_locations, container, false);

        //set button listener
        _rootView.findViewById(R.id.add).setOnClickListener(addListener);
        _rootView.findViewById(R.id.btn_add_location).setOnClickListener(addListener);

        //initialise data array
        _listData = new ArrayList<LocationListItem>();

        // get the enhanced listview
        EnhancedListView listView = (EnhancedListView) _rootView.findViewById(R.id.lstLocations);

        //get adapter
        _listAdapter = new LocationListAdapter(getActivity(), _listData);

        // set list adapter
        listView.setAdapter(_listAdapter);

        //get data from content provider
        getLoaderManager().initLoader(0, null, this);

        //set dismiss listener to handle swipe deletion
        listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, final int position) {
                final LocationListItem item = (LocationListItem) _listAdapter.getItem(position);
                _listAdapter.remove(position);
                return new EnhancedListView.Undoable() {
                    // Reinsert the item to the adapter
                    @Override
                    public void undo() {
                        _listAdapter.insert(position, item);
                        _listAdapter.notifyDataSetChanged();
                    }
                    // Return a string for your item
                    @Override
                    public String getTitle() {
                        return "Deleted " + item.getAddress();
                    }
                    // Delete item completely from your persistent storage
                    @Override
                    public void discard() {
                        try {
                            Uri singleLoc = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_LOCATIONS, item.getID());
                            getActivity().getContentResolver().delete(singleLoc, null, null);
                        }
                        catch (SQLiteConstraintException e)
                        {
                            undo();
                            Toast.makeText(getActivity().getApplicationContext(), "Unable to delete location, a reminder is using it", Toast.LENGTH_LONG).show();
                        }
                    }
                };
            }
        });

        //enhanced list view config settings
        listView.setRequireTouchBeforeDismiss(false);
        listView.enableSwipeToDismiss();
        listView.setSwipeDirection(EnhancedListView.SwipeDirection.START);

        return _rootView;
    }

    //reload data on resume
    @Override
    public void onResume() {
        super.onResume();
        _listData.clear();
        getLoaderManager().restartLoader(0, null, this);
    }

    //open location dialog on click
    View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

           FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
           // For a little polish, specify a transition animation
           transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
           NewLocationDialogFragment addNew = new NewLocationDialogFragment();
           transaction.replace(R.id.mainContent, addNew)
                   .addToBackStack(null).commit();
            DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    };

    //load all locations
    @Override
    public android.support.v4.content.Loader onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader loader = new android.support.v4.content.CursorLoader(getActivity(),
                ReminderContentProvider.CONTENT_URI_LOCATIONS,
                null,
                null,
                null,
                null);
        return loader;
    }

    //display locations
    @Override
    public void onLoadFinished(android.support.v4.content.Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        //display add card if no locations found
        if (cursor.getCount() == 0) {
            _rootView.findViewById(R.id.card_welcome).setVisibility(View.VISIBLE);
        }
        //else hide it
        else
        {
            _rootView.findViewById(R.id.card_welcome).setVisibility(View.INVISIBLE);
        }
        while (cursor.moveToNext()) {
            _listData.add(new LocationListItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4)));
        }
        cursor.close();
        _listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader loader) {
    }
}
