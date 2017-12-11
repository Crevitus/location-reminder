package com.crevitus.locationreminder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.receiver.TimedReminderReceiver;

public class TimedReminderService {

    public static void setTimedReminder(Context context, Reminder reminder)
    {
        Intent intent = new Intent(context, TimedReminderReceiver.class);
        intent.putExtra("lID", reminder.getLID());
        intent.putExtra("rID", reminder.getRID());

        PendingIntent pi = PendingIntent.getBroadcast(context, reminder.getLID(), intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        //set appropriate alarm
        if(Build.VERSION.SDK_INT >= 19)
        {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.getStartTime(), pi);
        }
        else
        {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getStartTime(),pi);
        }
    }


    public static void removeTimedReminder(Context context, Reminder reminder)
    {
        //send out matching pending intent to cancel the alarm
        Intent intent = new Intent(context, TimedReminderReceiver.class);
        intent.putExtra("lID", reminder.getLID());
        intent.putExtra("rID", reminder.getRID());
        PendingIntent sender = PendingIntent.getBroadcast(context, reminder.getRID(), intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
