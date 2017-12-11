package com.crevitus.locationreminder.UI;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.adapter.ReminderListAdapter;
import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.model.ReminderListItem;
import com.crevitus.locationreminder.provider.ReminderContentProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.timroes.android.listview.EnhancedListView;

public class HistoryFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks {
    private List<ReminderListItem> _listData;
    private ReminderListAdapter _listAdapter;
    private View _rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.fragment_history, container, false);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("History");

        //initialise data array
        _listData = new ArrayList<ReminderListItem>();

        // get the enhanced listview (allows swipe to delete)
        EnhancedListView listView = (EnhancedListView) _rootView.findViewById(R.id.lstReminders);

        //get adapter
        _listAdapter = new ReminderListAdapter(getActivity(), _listData);

        // set list adapter
        listView.setAdapter(_listAdapter);

        //set listview onclick listener to view reminder
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ReminderListItem item = (ReminderListItem) _listAdapter.getItem(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), ViewActivity.class);
                intent.putExtra("delete", 1);
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

        //listener to handle swipe to delete
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
                    // Delete item completely from your persistent storage
                    @Override
                    public void discard() {
                        //delete the reminder from the database
                        Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_REMINDERS, item.getRID());
                        getActivity().getContentResolver().delete(singleRem, null, null);
                    }
                };
            }
        });

        //set enhanced listview configs
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

    //get all disabled reminders
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
                    new String[]{Reminder.STATE_DISABLED},
                    null);
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        String dateTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        while (cursor.moveToNext()) {
            //get readable datestring if it exists
            if (cursor.getLong(3) != 0) {
                cal.setTimeInMillis(cursor.getLong(3));
                dateTime = formatter.format(cal.getTime());
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
                        new String[]{ReminderContentProvider.KEY_ADDRESS},
                        null, null, null);
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

