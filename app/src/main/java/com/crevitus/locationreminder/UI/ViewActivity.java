package com.crevitus.locationreminder.UI;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.model.Reminder;
import com.crevitus.locationreminder.provider.ReminderContentProvider;
import com.crevitus.locationreminder.utils.ReminderUtils;

public class ViewActivity extends ActionBarActivity {
    private Toolbar mToolbar;
    private Intent _contentIntent;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.primaryDark));
        }

        _contentIntent = getIntent();

        TextView txtNote = (TextView) findViewById(R.id.txtNote);
        TextView txtTitle = (TextView) findViewById(R.id.toolbar_title);
        TextView txtAddress = (TextView) findViewById(R.id.txtAddress);
        TextView txtTime = (TextView) findViewById(R.id.txtTime);

        txtTitle.setText(_contentIntent.getStringExtra("title"));
        txtNote.setText(_contentIntent.getStringExtra("note"));
        txtAddress.setText(_contentIntent.getStringExtra("address"));
        txtTime.setText(_contentIntent.getStringExtra("time"));
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reminder_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if(_contentIntent.getIntExtra("fired", 0) == 0)
                {
                    ReminderUtils.removeReminder(this, _contentIntent.getIntExtra("rID", 0));
                }
                if(_contentIntent.getIntExtra("delete", 0) == 1)
                {
                    Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_REMINDERS, _contentIntent.getIntExtra("rID", 0));
                    getContentResolver().delete(singleRem, null, null);
                }
                Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return true;
            case R.id.action_reset:
                Uri singleRem = ContentUris.withAppendedId(ReminderContentProvider.CONTENT_URI_REMINDERS, _contentIntent.getIntExtra("rID", 0));
                ContentValues values = new ContentValues();
                values.put(ReminderContentProvider.KEY_ENABLED, Reminder.STATE_ENABLED);
                getContentResolver().update(singleRem, values, null, null);
                ReminderUtils.addReminder(this, _contentIntent.getIntExtra("rID", 0));
                Toast.makeText(this, "Reminder reset", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
