package com.crevitus.locationreminder.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crevitus.locationreminder.R;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NewReminderDialogFragment extends DialogFragment implements CalendarDatePickerDialog
        .OnDateSetListener, RadialTimePickerDialog.OnTimeSetListener  {

    private static final String FRAG_TAG_DATE_PICKER = "datepickerdialog";
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialog";
    private TextView _datePicker, _timePicker;
    private Calendar _cal = Calendar.getInstance();
    private View _rootView;
    private boolean _timeEnabled = false, _dateSet = false, _timeSet = false;
    private CheckBox _chkArriving, _chkLeaving;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.dialog_add_new_reminder, container, false);

        Toolbar toolbar = (Toolbar) _rootView.findViewById(R.id.toolbar);

        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Add Reminder");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

       final EditText repetition = (EditText) _rootView.findViewById(R.id.edtRepeatAmount);

        repetition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repetition.setSelection(0, repetition.getText().length());
            }
        });


        repetition.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(repetition.length() == 0)
                {
                    repetition.setText("0");
                }
            }
        });


        Button btnAddNew = (Button) _rootView.findViewById(R.id.btn_add_location);
        Button btnlstLoc = (Button) _rootView.findViewById(R.id.btn_saved);
        btnAddNew.setOnClickListener(btnListener);
        btnlstLoc.setOnClickListener(btnListener);

        TextView txtTime = (TextView) _rootView.findViewById(R.id.txtTimeTitle);

        _chkArriving = (CheckBox) _rootView.findViewById(R.id.chkArriving);
        _chkLeaving = (CheckBox) _rootView.findViewById(R.id.chkLeaving);
        _chkArriving.setOnClickListener(chkListener);
        _chkLeaving.setOnClickListener(chkListener);

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               RelativeLayout dContainer = (RelativeLayout) _rootView.findViewById(R.id.datetimeContainer);
               if(dContainer.getVisibility() == View.INVISIBLE)
               {
                   closeKeyboard();
                   _timeEnabled = true;
                   dContainer.setVisibility(View.VISIBLE);
               }
               else
               {
                   _timeEnabled = false;
                   dContainer.setVisibility(View.INVISIBLE);
               }
            }
        });

        _datePicker = (TextView) _rootView.findViewById(R.id.txtDate);
        _timePicker = (TextView) _rootView.findViewById(R.id.txtTime);

        _datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                        .newInstance(NewReminderDialogFragment.this, _cal.get(Calendar.YEAR),
                        _cal.get(Calendar.MONTH),
                                _cal.get(Calendar.DAY_OF_MONTH));
                calendarDatePickerDialog.show(fm, FRAG_TAG_DATE_PICKER);
            }
        });

        _timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                        .newInstance(NewReminderDialogFragment.this,
                                _cal.get(Calendar.HOUR_OF_DAY), _cal.get(Calendar.MINUTE),
                                DateFormat.is24HourFormat(getActivity()));

                    timePickerDialog.show(getActivity().getSupportFragmentManager(), FRAG_TAG_TIME_PICKER);
                }
        });

        return _rootView;
    }

    @Override
    public void onResume() {
        // reattach dialogs
        super.onResume();
        CalendarDatePickerDialog calendarDatePickerDialog = (CalendarDatePickerDialog)
                getActivity().getSupportFragmentManager()
                        .findFragmentByTag(FRAG_TAG_DATE_PICKER);
        if (calendarDatePickerDialog != null) {
            calendarDatePickerDialog.setOnDateSetListener(this);
        }

        RadialTimePickerDialog rtpd = (RadialTimePickerDialog)
                getActivity().getSupportFragmentManager()
                        .findFragmentByTag(
                                FRAG_TAG_TIME_PICKER);
        if (rtpd != null) {
            rtpd.setOnTimeSetListener(this);
        }
    }

    @Override
    public void onDestroyView() {
        if(getActivity() instanceof DialogCloseListener) {
            ((DialogCloseListener) getActivity()).handleDialogClose();
        }
        super.onDestroyView();
    }

    View.OnClickListener chkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == _chkArriving.getId()) {
                if(_chkArriving.isChecked()) {
                    _chkLeaving.setChecked(false);
                }
            }
            else if(_chkArriving.isChecked()) {
                _chkArriving.setChecked(false);
            }
        }
    };

    private void closeKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null :
                getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private boolean checkInput(EditText editText)
    {
        if(editText.getText().length() == 0)
        {
            editText.setError("Field cannot be left blank.");
            return false;
        }
        else
        {
            editText.setError(null);
            return true;
        }
    }

    View.OnClickListener btnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            closeKeyboard();
            Fragment frag;
            Bundle bundle = new Bundle();
            EditText remTitle = (EditText) _rootView.findViewById(R.id.edtRemTitle);
            EditText remNote = (EditText) _rootView.findViewById(R.id.edtRemNote);
            if(!checkInput(remTitle) || !checkInput(remNote)) {
                return;
            }
            bundle.putString("title", remTitle.getText().toString());
            bundle.putString("message", remNote.getText().toString());
            if(_timeEnabled)
            {
                EditText edtRepetition = (EditText) _rootView.findViewById(R.id.edtRepeatAmount);
                Spinner sRepetition = (Spinner) _rootView.findViewById(R.id.repetitonOpt);
                Long repetition = Long.parseLong(edtRepetition.getText().toString()) * getTimeType(sRepetition.getSelectedItem().toString());
                if(!_dateSet || !_timeSet)
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Please set a date and time", Toast.LENGTH_SHORT).show();
                    return;
                }
                bundle.putLong("dateTime", _cal.getTimeInMillis());
                if(repetition != 0)
                {
                    bundle.putLong("repetition", repetition);
                }
            }
            CheckBox chkArriving = (CheckBox) _rootView.findViewById(R.id.chkArriving);
            CheckBox chkLeaving = (CheckBox) _rootView.findViewById(R.id.chkLeaving);
            if(chkArriving.isChecked())
            {
                bundle.putString("type", "arriving");
            }
            else if(chkLeaving.isChecked())
            {
                bundle.putString("type", "leaving");
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "Please choose a reminder type", Toast.LENGTH_SHORT).show();
                return;
            }
            if(v.getId() == R.id.btn_add_location)
            {
                frag = new NewLocationDialogFragment();
            }
            else{
                frag = new LocationListDialogFragment();
            }
            newDialog(frag, bundle);
        }
    };

    private long getTimeType(String type)
    {
        long tLength = 0;
        switch (type)
        {
            case "Hour(s)":
                tLength = TimeUnit.HOURS.toMillis(1);
                break;
            case "Days(s)":
                tLength = TimeUnit.DAYS.toMillis(1);
                break;
            case "Week(s)":
                tLength = TimeUnit.DAYS.toMillis(1) * 7;
                break;
        }
        return tLength;
    }

    private void newDialog(Fragment frag, Bundle bundle){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        frag.setArguments(bundle);

        // For a little polish, specify a transition animation
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.mainContent, frag, "dialog")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year,  int monthOfYear, int dayOfMonth)
    {
        _cal.set(Calendar.YEAR, year);
        _cal.set(Calendar.MONTH, monthOfYear);
        _cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        _cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());

        _datePicker.setText("" + dayOfMonth +
                " " + _cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH) +
                " " + year);

        _dateSet = true;

        }

    @Override
    public void onTimeSet(RadialTimePickerDialog radialTimePickerDialog, int hourOfDay, int minute) {
        _cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        _cal.set(Calendar.MINUTE, minute);

        String curTime = String.format("%02d:%02d", hourOfDay, minute);

        if(!DateFormat.is24HourFormat(getActivity()))
        {
            int tempAm_PM = _cal.get(Calendar.AM_PM);
            String AM_PM;
            if(tempAm_PM == Calendar.AM){
                AM_PM = " am";
            }
            else
            {
                AM_PM = " pm";
            }
            curTime = String.format("%02d:%02d", _cal.get(Calendar.HOUR), minute);
            _timePicker.setText(curTime + AM_PM);
        }
        else
        {
            _timePicker.setText(curTime);
        }
        _timeSet = true;
    }
}