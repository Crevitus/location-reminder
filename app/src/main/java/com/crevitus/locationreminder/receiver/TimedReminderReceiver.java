package com.crevitus.locationreminder.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.provider.ReminderContentProvider;
import com.crevitus.locationreminder.service.TimedReminderService;
import com.crevitus.locationreminder.utils.GeofenceUtils;
import com.google.android.gms.maps.model.LatLng;

public class TimedReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri singleLoc = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_LOCATIONS, intent.getIntExtra("lID", 1));
        Cursor locCursor = context.getContentResolver().query(singleLoc,
                new String[] {ReminderContentProvider.KEY_LID,
                        ReminderContentProvider.KEY_LAT,
                        ReminderContentProvider.KEY_LNG,
                        ReminderContentProvider.KEY_RADIUS},
                null, null, null);
        while(locCursor.moveToNext())
        {
            Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_REMINDERS, intent.getIntExtra("rID", 1));
            Cursor remCursor = context.getContentResolver().query(singleRem,
                    new String[] {ReminderContentProvider.KEY_RID,
                            ReminderContentProvider.KEY_DATETIME,
                            ReminderContentProvider.KEY_TYPE,
                            ReminderContentProvider.KEY_REPETITION},
                    null, null, null);
            while(remCursor.moveToNext()) {
                //add geo fence
                GeofenceUtils geo = new GeofenceUtils(context);
                geo.addGeoFence(new Reminder(null,
                        remCursor.getInt(0),
                        new LatLng(Double.parseDouble(locCursor.getString(1)),
                                Double.parseDouble(locCursor.getString(2))),
                        locCursor.getInt(3),
                        remCursor.getString(2),
                        null));
                //if repeat interval > 0, set another timed reminder
                if(remCursor.getLong(3) != 0) {
                    TimedReminderService.setTimedReminder(context, new Reminder(locCursor.getInt(0),
                            remCursor.getInt(0),
                            null,
                            null,
                            null,
                            remCursor.getLong(1) + remCursor.getLong(3)));
                    //update the reminder with the new start datetime
                    ContentValues values = new ContentValues();
                    values.put(ReminderContentProvider.KEY_DATETIME, remCursor.getLong(1) + remCursor.getLong(3));
                    context.getContentResolver().update(singleRem, values, null, null);
                }
            }
            remCursor.close();
        }
        locCursor.close();
    }
}
