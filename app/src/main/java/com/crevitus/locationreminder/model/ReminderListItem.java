package com.crevitus.locationreminder.model;

public class ReminderListItem {
    private String _title, _note, _address, _dateTime;
    private int _rID, _lID;

    //data model for list items displaying reminders
    public ReminderListItem(int rID, int lID, String title, String note, String address, String dateTime)
    {
        _rID = rID;
        _lID = lID;
        _title = title;
        _note = note;
        _address = address;
        _dateTime = dateTime;
    }

    public int getRID() {
        return _rID;
    }

    public int getLID() {
        return _lID;
    }

    public String getTitle() {
        return _title;
    }

    public String getNote() {
        return _note;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String address)
    {
        _address = address;
    }

    public String getDateTime() {
        return _dateTime;
    }
}
