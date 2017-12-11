package com.crevitus.locationreminder.UI;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.adapter.ReminderListAdapter;
import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.model.ReminderListItem;
import com.crevitus.locationreminder.provider.ReminderContentProvider;
import com.crevitus.locationreminder.utils.ReminderUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.timroes.android.listview.EnhancedListView;

public class MainFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks {
    private List<ReminderListItem> _listData;
    private ReminderListAdapter _listAdapter;
    private View _rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Location Reminder");

        Button btnAddReminder = (Button) _rootView.findViewById(R.id.btn_add_reminder);
        Button btnAddLocation = (Button) _rootView.findViewById(R.id.btn_add_location);
        ImageView imageAdd = (ImageView) _rootView.findViewById(R.id.add);
        btnAddReminder.setOnClickListener(addListener);
        btnAddLocation.setOnClickListener(addListener);
        imageAdd.setOnClickListener(addListener);

        //initialise data array
        _listData = new ArrayList<ReminderListItem>();

        // get the enhanced listview
        EnhancedListView listView = (EnhancedListView) _rootView.findViewById(R.id.lstReminders);

        //get adapter
        _listAdapter = new ReminderListAdapter(getActivity(), _listData);

        // set list adapter
        listView.setAdapter(_listAdapter);

        //display reminder onclick
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ReminderListItem item = (ReminderListItem) _listAdapter.getItem(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), ViewActivity.class);
                intent.putExtra("rID", item.getRID());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("time", item.getDateTime());
                intent.putExtra("note", item.getNote());
                intent.putExtra("address", item.getAddress());
                startActivity(intent);
            }
        });

        //get data from content provider
        getLoaderManager().initLoader(0, null, this);

        //handle swipe dismiss to delete
        listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, final int position) {
                final ReminderListItem item = (ReminderListItem) _listAdapter.getItem(position);
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
                        return "Deleted " + item.getTitle();
                    }

                    //disable reminder (non permanent delete)
                    @Override
                    public void discard() {
                        ReminderUtils.removeReminder(getActivity().getApplicationContext(), item.getRID());
                    }
                };
            }
        });

        //set enhanced listview config settings
        listView.setRequireTouchBeforeDismiss(false);
        listView.enableSwipeToDismiss();
        listView.setSwipeDirection(EnhancedListView.SwipeDirection.START);

        return _rootView;
    }

    //reload data
    @Override
    public void onResume() {
        super.onResume();
        _listData.clear();
        getLoaderManager().restartLoader(0, null, this);
    }

    //open correct dialog on button click
    View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Fragment frag;

            if (v.getId() == R.id.btn_add_location) {
                frag = new NewLocationDialogFragment();
            } else {
                frag = new NewReminderDialogFragment();
            }

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }

            // For a little polish, specify a transition animation
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            ft.replace(R.id.mainContent, frag, "dialog")
                    .addToBackStack(null)
                    .commit();
        }
    };

    //get enabled reminders
    @Override
    public android.support.v4.content.Loader onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader loader = new android.support.v4.content.CursorLoader(getActivity(),
                ReminderContentProvider.CONTENT_URI_REMINDERS,
                new String[] {ReminderContentProvider.KEY_RID,
                        ReminderContentProvider.KEY_REMINDER_TITLE,
                        ReminderContentProvider.KEY_REMINDER_MESSAGE,
                        ReminderContentProvider.KEY_DATETIME,
                        ReminderContentProvider.KEY_LOCATION_ID},
                ReminderContentProvider.KEY_ENABLED + "=?",
                new String[]{Reminder.STATE_ENABLED},
                null);
        return loader;
    }

    //
    @Override
    public void onLoadFinished(android.support.v4.content.Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        String dateTime = "";
        Calendar cal = Calendar.getInstance();
        //display add card if no reminders found
        if (cursor.getCount() == 0) {
            _rootView.findViewById(R.id.card_welcome).setVisibility(View.VISIBLE);
        }
        //else hide card
        else
        {
            _rootView.findViewById(R.id.card_welcome).setVisibility(View.INVISIBLE);
        }
        while (cursor.moveToNext()) {
            //get readable datestring if it exists
            if (cursor.getLong(3) != 0) {
                cal.setTimeInMillis(cursor.getLong(3));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                dateTime = sdf.format(cal.getTime());
            }
            else{
                dateTime = "";
            }
            _listData.add(new ReminderListItem(
                    cursor.getInt(0),
                    cursor.getInt(4),
                    cursor.getString(1),
                    cursor.getString(2),
                    null,
                    dateTime));
        }
        cursor.close();
        //get the address of the reminders in an async task
        new GetAddressTask().execute(_listData);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader loader) {
    }

    private class GetAddressTask extends AsyncTask<List<ReminderListItem>, Integer, String> {

        @Override
        protected String doInBackground(List<ReminderListItem>... reminderList) {
            //get address from location table for every reminder in list
            for(ReminderListItem listItem : reminderList[0])
            {
                Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_LOCATIONS, listItem.getLID());
                Cursor cursor = getActivity().getContentResolver().query(singleRem,
                        new String[]{ReminderContentProvider.KEY_ADDRESS}, null, null, null);
                while(cursor.moveToNext())
                {
                    listItem.setAddress(cursor.getString(0));
                }
                cursor.close();
            }
            return "completed";
        }
        protected void onPostExecute(String result) {
           _listAdapter.notifyDataSetChanged();
        }
    }
}

