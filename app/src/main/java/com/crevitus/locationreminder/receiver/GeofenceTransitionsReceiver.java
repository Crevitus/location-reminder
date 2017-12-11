package com.crevitus.locationreminder.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.UI.ViewActivity;
import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.provider.ReminderContentProvider;
import com.crevitus.locationreminder.utils.GeofenceUtils;
import com.crevitus.locationreminder.utils.ReminderUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GeofenceTransitionsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //add all geofences again
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
            ReminderUtils.addAllReminders(context);
        }
        //else geofence triggered
        else {
                Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_REMINDERS, intent.getIntExtra("id", 0));
                Cursor remCursor = context.getContentResolver().query(singleRem,
                        new String[] { ReminderContentProvider.KEY_REMINDER_TITLE,
                                ReminderContentProvider.KEY_REMINDER_MESSAGE,
                                ReminderContentProvider.KEY_LOCATION_ID,
                                ReminderContentProvider.KEY_DATETIME},
                        null, null, null);
                while(remCursor.moveToNext()) {
                    GeofenceUtils utils = new GeofenceUtils(context);
                    utils.removeGeoFence(intent.getIntExtra("id", 0));

                    ContentResolver contentResolver = context.getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(ReminderContentProvider.KEY_ENABLED, Reminder.STATE_DISABLED);
                    contentResolver.update(singleRem, values, null, null);


                    //open view activity on notification click
                    Intent notificationIntent = new Intent(context, ViewActivity.class);

                    // Construct a task stack.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

                    // Add the main Activity to the task stack as the parent.
                    stackBuilder.addParentStack(ViewActivity.class);


                    Uri singleLoc = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_LOCATIONS, remCursor.getInt(2));
                    Cursor locCursor = context.getContentResolver().query(singleLoc, new String[] {ReminderContentProvider.KEY_ADDRESS}, null, null, null);
                    while(locCursor.moveToNext()) {
                        notificationIntent.putExtra("fired", 1);
                        notificationIntent.putExtra("rID", intent.getIntExtra("id", 1));
                        notificationIntent.putExtra("title", remCursor.getString(0));
                        if (remCursor.getLong(3) != 0) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(remCursor.getLong(3));
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                            notificationIntent.putExtra("time", sdf.format(cal.getTime()));
                        }
                        notificationIntent.putExtra("note", remCursor.getString(1));
                        notificationIntent.putExtra("address", locCursor.getString(0));
                    }
                    locCursor.close();

                    // Push the content Intent onto the stack.
                    stackBuilder.addNextIntent(notificationIntent);

                    PendingIntent notificationPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    //set notification
                    NotificationCompat.Builder notiClass = new NotificationCompat.Builder(context)
                            .setContentTitle(remCursor.getString(0))
                            .setContentText(remCursor.getString(1))
                            .setSmallIcon(R.drawable.ic_location)
                            .setContentIntent(notificationPendingIntent)
                            .setWhen(System.currentTimeMillis());

                    notiClass.setVibrate(new long[]{0, 200, 200, 200, 200, 200, 200});


                    notiClass.setDefaults(NotificationCompat.DEFAULT_SOUND);
                    notiClass.setLights(Color.WHITE, 700, 700);
                    notiClass.setAutoCancel(true);

                    NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    // Builds the notification and issues it.
                    notifyMgr.notify(intent.getIntExtra("id", 1), notiClass.build());
                }
            remCursor.close();
            }
        }
    }