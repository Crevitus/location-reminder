package com.crevitus.locationreminder.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.provider.ReminderContentProvider;
import com.crevitus.locationreminder.service.TimedReminderService;
import com.google.android.gms.maps.model.LatLng;

public class ReminderUtils {
    public static void removeReminder(Context context, int rID)
    {
        Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_REMINDERS, rID);
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ReminderContentProvider.KEY_ENABLED, Reminder.STATE_DISABLED);
        contentResolver.update(singleRem, values, null, null);

        //remove reminder type
        Cursor remCursor = contentResolver.query(singleRem, new String[]{ReminderContentProvider.KEY_DATETIME,
                ReminderContentProvider.KEY_LOCATION_ID}, null, null, null);
        while(remCursor.moveToNext())
        {
            if(remCursor.getInt(0) != 0) {
                TimedReminderService.removeTimedReminder(context, new Reminder(remCursor.getInt(1),
                        rID,
                        null, null, null, null));
            }
            GeofenceUtils utils = new GeofenceUtils(context);
            utils.removeGeoFence(rID);
        }
        remCursor.close();
    }

    public static void addAllReminders(Context context)
    {
        Cursor remCursor = context.getContentResolver().query(ReminderContentProvider.CONTENT_URI_REMINDERS,
                new String[] {ReminderContentProvider.KEY_RID},
                ReminderContentProvider.KEY_ENABLED + "=?",
                new String[]{Reminder.STATE_ENABLED},
                null, null);
        while(remCursor.moveToNext())
        {
            addReminder(context, remCursor.getInt(0));
        }
    }

    public static void addReminder(Context context, int rID)
    {
        Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_REMINDERS, rID);
        Cursor remCursor = context.getContentResolver().query(singleRem,
                new String[] {ReminderContentProvider.KEY_RID,
                        ReminderContentProvider.KEY_DATETIME,
                        ReminderContentProvider.KEY_REPETITION,
                        ReminderContentProvider.KEY_LOCATION_ID,
                        ReminderContentProvider.KEY_TYPE},
                null, null, null);
        while(remCursor.moveToNext())
        {
            Uri singleLoc = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_LOCATIONS, remCursor.getInt(3));
            Cursor locCursor = context.getContentResolver().query(singleLoc,
                    new String[] {ReminderContentProvider.KEY_LID,
                            ReminderContentProvider.KEY_LAT,
                            ReminderContentProvider.KEY_LNG,
                            ReminderContentProvider.KEY_RADIUS},
                    null, null, null);
            while(locCursor.moveToNext())
            {
                //if repeat datetime != 0, set timed reminder
                if(remCursor.getLong(1) != 0) {
                    TimedReminderService.setTimedReminder(context, new Reminder(locCursor.getInt(0),
                            remCursor.getInt(0),
                            null,
                            null,
                            null,
                            remCursor.getLong(1)));
                }
                else {
                    //add geo fence
                    GeofenceUtils geo = new GeofenceUtils(context);
                    geo.addGeoFence(new Reminder(null,
                            remCursor.getInt(0),
                            new LatLng(Double.parseDouble(locCursor.getString(1)),
                                    Double.parseDouble(locCursor.getString(2))),
                            locCursor.getInt(3),
                            remCursor.getString(4),
                            null));
                }
            }
            locCursor.close();
        }
        remCursor.close();
    }
}
