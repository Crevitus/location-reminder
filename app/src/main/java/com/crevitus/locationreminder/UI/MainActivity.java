package com.crevitus.locationreminder.UI;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.crevitus.locationreminder.R;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements DialogCloseListener {

    private String[] _drawerTitles;
    private ListView _drawerList;
    private LinearLayout _drawerContainer;
    private DrawerLayout _drawerLayout;
    private ActionBarDrawerToggle _drawerToggle;
    private ArrayList<NavItem> _navItems = new ArrayList<NavItem>();
    private Toolbar _toolbar;
    private CharSequence _title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //get draw items
        _drawerTitles = getResources().getStringArray(R.array.drawerItems);
        TypedArray drawerIcons = getResources().obtainTypedArray(R.array.drawerIcons);
        for(int i = 0; i < _drawerTitles.length; i++ ) {
            _navItems.add(new NavItem(_drawerTitles[i],drawerIcons.getResourceId(i, -1)));
        }
        drawerIcons.recycle();

        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerList = (ListView) findViewById(R.id.lstDrawerTitles);
        _drawerContainer = (LinearLayout)findViewById(R.id.drawer_container);
        _drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary));

        // Set the adapter for the list view
        DrawerListAdapter adapter = new DrawerListAdapter(this, _navItems);
        _drawerList.setAdapter(adapter);

        // Set the list's click listener
        _drawerList.setOnItemClickListener(new DrawerItemClickListener());

        //if app just opened set main content fragment
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.mainContent,
                    new MainFragment()).commit();
        }

        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout, _toolbar, R.string.app_name, R.string.app_name)
        {
            //handle the status bar changes on drawer state change
            @Override
            public void onDrawerStateChanged(int newState) {
                Window window = getWindow();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (newState == DrawerLayout.STATE_SETTLING) {
                        if (_drawerLayout.isDrawerOpen(GravityCompat.START))
                        {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            window.setStatusBarColor(getResources().getColor(R.color.primaryDark));
                        }
                        else
                        {
                            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            window.setStatusBarColor(Color.TRANSPARENT);
                        }
                    }
                    else if (newState == DrawerLayout.STATE_IDLE)
                    {
                        if (_drawerLayout.isDrawerOpen(GravityCompat.START))
                        {
                            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            window.setStatusBarColor(Color.TRANSPARENT);
                        }
                        else
                        {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            window.setStatusBarColor(getResources().getColor(R.color.primaryDark));
                        }
                    }
                    invalidateOptionsMenu();
                }
            }
        };
        _drawerLayout.setDrawerListener(_drawerToggle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        Fragment fragment = new Fragment();
        switch (position)
        {
            case 0:
                fragment = new MainFragment();
                setSupportActionBar(_toolbar);
                _drawerToggle.syncState();
                break;
            case 1:
                fragment = new LocationsFragment();
                setSupportActionBar(_toolbar);
                _drawerToggle.syncState();
                break;
            case 2:
                fragment = new HistoryFragment();
                setSupportActionBar(_toolbar);
                _drawerToggle.syncState();
                break;
            default:
                break;
        }
        // Insert the fragment by replacing any existing fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        _drawerList.setItemChecked(position, true);
        setTitle(_drawerTitles[position]);
        _drawerLayout.closeDrawer(_drawerContainer);
    }

    @Override
    public void handleDialogClose() {
        _drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void setTitle(CharSequence title) {
        _title = title;
        getSupportActionBar().setTitle(_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
                return super.onContextItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (_drawerLayout.isDrawerOpen(Gravity.START | Gravity.LEFT)) {
            _drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    //private draw list adapter
    private class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_list_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.drawer_text);
            ImageView iconView = (ImageView) view.findViewById(R.id.drawer_icon);

            titleView.setText( mNavItems.get(position).mTitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            return view;
        }
    }

    private class NavItem {
        String mTitle;
        int mIcon;

        public NavItem(String title, int icon) {
            mTitle = title;
            mIcon = icon;
        }
    }
}

