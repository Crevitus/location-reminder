package com.crevitus.locationreminder.provider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

public class ReminderContentProvider extends ContentProvider {
	
	//Table Column names
	//The index (key) column name for use in where clauses.
    //Reminder table
	public static final String KEY_RID = "_RID";
    public static final String KEY_REMINDER_TITLE = "REMINDER_TITLE";
    public static final String KEY_REMINDER_MESSAGE = "REMINDER_MESSAGE";
    public static final String KEY_DATETIME = "DATETIME";
    public static final String KEY_REPETITION = "REPETITION";
    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_LOCATION_ID = "LOCATION_ID";
    public static final String KEY_ENABLED = "ENABLED";


    //locations table
    public static final String KEY_LID = "_LID";
    public static final String KEY_LAT = "LAT";
    public static final String KEY_LNG = "LNG";
    public static final String KEY_RADIUS = "RADIUS";
    public static final String KEY_ADDRESS = "ADDRESS";
    
    //the data URI's
    public static final Uri CONTENT_URI_REMINDERS =
    		Uri.parse("content://com.crevitus.locationreminder/reminders");
    public static final Uri CONTENT_URI_LOCATIONS =
            Uri.parse("content://com.crevitus.locationreminder/locations");

    private ReminderDBOpenHelper _reminderDBOpenHelper;
    private static final int REMINDERS = 1;
    private static final int REMINDER_ID = 2;
    private static final int LOCATIONS = 3;
    private static final int LOCATION_ID = 4;
    
    private static final UriMatcher uriMatcher;
    
    static {
    	uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	uriMatcher.addURI("com.crevitus.locationreminder", "reminders", REMINDERS);
    	uriMatcher.addURI("com.crevitus.locationreminder", "reminders/#", REMINDER_ID);
        uriMatcher.addURI("com.crevitus.locationreminder", "locations", LOCATIONS);
        uriMatcher.addURI("com.crevitus.locationreminder", "locations/#", LOCATION_ID);
    }

	@Override
	public boolean onCreate() {
		_reminderDBOpenHelper = new ReminderDBOpenHelper(getContext(), ReminderDBOpenHelper.DATABASE_NAME, null,
                ReminderDBOpenHelper.DATABASE_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		//open read only database
		SQLiteDatabase db = _reminderDBOpenHelper.getReadableDatabase();
		String groupBy = null;
		String having = null;
		
		SQLiteQueryBuilder querybuilder = new SQLiteQueryBuilder();

		switch(uriMatcher.match(uri))
		{
            case REMINDERS:
                querybuilder.setTables(ReminderDBOpenHelper.DATABASE_TABLE1);
                break;
            case REMINDER_ID:
                querybuilder.setTables(ReminderDBOpenHelper.DATABASE_TABLE1);
                querybuilder.appendWhere(KEY_RID + "=" + uri.getLastPathSegment());
                break;
            case LOCATIONS:
                querybuilder.setTables(ReminderDBOpenHelper.DATABASE_TABLE2);
                break;
            case LOCATION_ID:
                querybuilder.setTables(ReminderDBOpenHelper.DATABASE_TABLE2);
                querybuilder.appendWhere(KEY_LID + "=" + uri.getLastPathSegment());
                break;
		}
		return querybuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);

	}

	@Override
	public String getType(Uri uri) {
		//identify MIME type
		switch(uriMatcher.match(uri))
		{
			case REMINDERS: return "vnd.android.cursor.dir/vnd.crevitus.locationreminder/reminders";
			case REMINDER_ID: return "vnd.android.cursor.item/vnd.crevitus" +
                    ".locationreminder/reminders";
            case LOCATIONS: return "vnd.android.cursor.dir/vnd.crevitus.locationreminder/locations";
            case LOCATION_ID: return "vnd.android.cursor.item/vnd.crevitus" +
                    ".locationreminder/locations";
			default: return null;	
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = _reminderDBOpenHelper.getWritableDatabase();
		Uri insertedID = null;
        switch(uriMatcher.match(uri)) {
            case REMINDERS:
                long id1 = db.insert(ReminderDBOpenHelper.DATABASE_TABLE1, null, values);
                if (id1> -1) {
                    insertedID = ContentUris.withAppendedId(CONTENT_URI_REMINDERS, id1);
                    getContext().getContentResolver().notifyChange(insertedID, null);
                }
                break;
            case LOCATIONS:
                long id2 = db.insert(ReminderDBOpenHelper.DATABASE_TABLE2, null, values);
                if (id2 > -1) {
                    insertedID = ContentUris.withAppendedId(CONTENT_URI_LOCATIONS, id2);
                    getContext().getContentResolver().notifyChange(insertedID, null);
                }
                break;
        }
		return insertedID;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = _reminderDBOpenHelper.getWritableDatabase();
        int deleteCount = 0;
        switch (uriMatcher.match(uri))
        {
            case REMINDERS:
                deleteCount = db.delete(ReminderDBOpenHelper.DATABASE_TABLE1, selection,
                        selectionArgs);
                break;
            case REMINDER_ID:
                String rid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    deleteCount = db.delete(ReminderDBOpenHelper.DATABASE_TABLE1,
                            ReminderContentProvider.KEY_RID + "=" + rid,
                            null);
                } else {
                    deleteCount = db.delete(ReminderDBOpenHelper.DATABASE_TABLE1,
                            ReminderContentProvider.KEY_RID + "=" + rid
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            case LOCATIONS:
                deleteCount = db.delete(ReminderDBOpenHelper.DATABASE_TABLE1, selection,
                        selectionArgs);
                break;
            case LOCATION_ID:
                String lid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    deleteCount = db.delete(ReminderDBOpenHelper.DATABASE_TABLE2,
                            ReminderContentProvider.KEY_LID + "=" + lid,
                            null);
                } else {
                    deleteCount = db.delete(ReminderDBOpenHelper.DATABASE_TABLE2,
                            ReminderContentProvider.KEY_LID + "=" + lid
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
        }

		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        SQLiteDatabase db = _reminderDBOpenHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri))
        {
            case REMINDERS:
                rowsUpdated = db.update(ReminderDBOpenHelper.DATABASE_TABLE1,
                        values,
                        selection,
                        selectionArgs);
                break;
            case REMINDER_ID:
                String rid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(ReminderDBOpenHelper.DATABASE_TABLE1,
                            values,
                            ReminderContentProvider.KEY_RID + "=" + rid,
                            null);
                } else {
                    rowsUpdated = db.update(ReminderDBOpenHelper.DATABASE_TABLE1,
                            values,
                            ReminderContentProvider.KEY_RID + "=" + rid
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            case LOCATIONS:
                rowsUpdated = db.update(ReminderDBOpenHelper.DATABASE_TABLE2,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LOCATION_ID:
                String lid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(ReminderDBOpenHelper.DATABASE_TABLE2,
                            values,
                            ReminderContentProvider.KEY_LID + "=" + lid,
                            null);
                } else {
                    rowsUpdated = db.update(ReminderDBOpenHelper.DATABASE_TABLE2,
                            values,
                            ReminderContentProvider.KEY_LID + "=" + lid
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
	}
	
	private static class ReminderDBOpenHelper extends SQLiteOpenHelper {
	    
	    private static final String DATABASE_NAME = "Reminders.db";
	    private static final String DATABASE_TABLE1 = "Reminders";
        private static final String DATABASE_TABLE2 = "Locations";
	    private static final int DATABASE_VERSION = 1;
	    
	    // SQL Statement to create a new database.
	    private static final String DATABASE_CREATE1 = "create table " +
	      DATABASE_TABLE1 + " (" + KEY_RID + " integer primary key autoincrement, " +
	      KEY_REMINDER_TITLE + " VARCHAR(50), " +
	      KEY_REMINDER_MESSAGE + " TEXT, " +
	      KEY_DATETIME + " INT, " +
          KEY_TYPE + " VARCHAR(10), " +
          KEY_REPETITION + " INT, " +
          KEY_LOCATION_ID + " INT, " +
          KEY_ENABLED + " VARCHAR(5), " +
          "FOREIGN KEY(" + KEY_LOCATION_ID + ") REFERENCES " + DATABASE_TABLE2 + "(" + KEY_LID + ")" + ");";

        private static final String DATABASE_CREATE2 = "create table " +
                DATABASE_TABLE2 + " (" + KEY_LID + " integer primary key autoincrement, " +
                KEY_LAT + " VARCHAR(30), " +
                KEY_LNG + " VARCHAR(30), " +
                KEY_RADIUS + " INT, " +
                KEY_ADDRESS + " TEXT" + ");";
	
	    public ReminderDBOpenHelper(Context context, String name,
                                    CursorFactory factory, int version) {
	      super(context, name, factory, version);
	    }
	
	    // Called when no database exists in disk and the helper class needs
	    // to create a new one.
	    @Override
	    public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE1);
            db.execSQL(DATABASE_CREATE2);

	    }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                if (! db.isReadOnly()) {
                    db.execSQL("PRAGMA foreign_keys = ON;");
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }
	
	    // Called when there is a database version mismatch meaning that
	    // the version of the database on disk needs to be upgraded to
	    // the current version.
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, 
	                          int newVersion) {
	    	
	      db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE1);
          db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE2);
	      // Create a new one.
	      onCreate(db);
	    }
	}
}
