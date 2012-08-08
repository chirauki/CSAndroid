package org.chirauki.CSAndroid;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CSAndroidDbAdapter {

    public static final String KEY_NAME = "name";
    public static final String KEY_URL = "url";
    public static final String KEY_APIK = "apikey";
    public static final String KEY_SECK = "secretkey";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASS = "password";
    public static final String KEY_FIRSTNAME = "firstname";
    public static final String KEY_LASTNAME = "lastname";
    public static final String KEY_USRTYPE = "usertype";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "CSAndroidDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table clouds (_id integer primary key autoincrement, "
		+ "name text not null, "
		+ "url text not null, "
		+ "apikey text not null, "
		+ "secretkey text not null, "
		+ "username text not null, "
		+ "password text not null, "
		+ "firstname text not null, "
		+ "lastname text not null, "
		+ "usertype text not null);";

    private static final String DATABASE_NAME = "csandroid";
    private static final String DATABASE_TABLE = "clouds";
    private static final int DATABASE_VERSION = 5;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS clouds");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public CSAndroidDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
	
    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public CSAndroidDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createCloud(String name, String url, String username, String password, String apikey, String secretkey) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_URL, url);
        initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_PASS, password);
        initialValues.put(KEY_APIK, apikey);
        initialValues.put(KEY_SECK, secretkey);
        
        CSAPIexecutor client = new CSAPIexecutor(url, apikey, secretkey);
        //String username = "";
        String firstname = "";
        String lastname = "";
        JSONObject user = client.whoAmI();
        try {
        	//username = user.getString("username");
	        firstname = user.getString("firstname");
	        lastname = user.getString("lastname");
        } catch (JSONException e) {
        	e.printStackTrace();
        }
        
        /*
         * account types are 
         * 1 (admin), 
         * 2 (domain-admin), and 
         * 0 (user).
         */
        String usertype = "";
        switch (client.getACC_TYPE()) {
        case 0:
        	usertype = "User"; break;
        case 1:
        	usertype = "Global Admin"; break;
        case 2:
        	usertype = "Domain Admin"; break;
        } 
        
        //initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_FIRSTNAME, firstname);
        initialValues.put(KEY_LASTNAME, lastname);
        initialValues.put(KEY_USRTYPE, usertype);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCloud(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllClouds() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,
                KEY_URL, KEY_APIK, KEY_SECK, KEY_USERNAME, KEY_PASS, KEY_FIRSTNAME, KEY_LASTNAME, KEY_USRTYPE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCloud(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,
                    KEY_URL, KEY_APIK, KEY_SECK, KEY_USERNAME, KEY_PASS, KEY_FIRSTNAME, KEY_LASTNAME, KEY_USRTYPE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateCloud(long rowId, String name, String url, String apikey, String secretkey) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_URL, url);
        args.put(KEY_APIK, apikey);
        args.put(KEY_SECK, secretkey);

        CSAPIexecutor client = new CSAPIexecutor(url, apikey, secretkey);
        String username = "";
        String firstname = "";
        String lastname = "";
        JSONObject user = client.whoAmI();
        try {
        	username = user.getString("username");
	        firstname = user.getString("firstname");
	        lastname = user.getString("lastname");
        } catch (JSONException e) {
        	e.printStackTrace();
        }
        
        /*
         * account types are 
         * 1 (admin), 
         * 2 (domain-admin), and 
         * 0 (user).
         */
        String usertype = "";
        switch (client.getACC_TYPE()) {
        case 0:
        	usertype = "User"; break;
        case 1:
        	usertype = "Global Admin"; break;
        case 2:
        	usertype = "Domain Admin"; break;
        } 
        
        args.put(KEY_USERNAME, username);
        args.put(KEY_FIRSTNAME, firstname);
        args.put(KEY_LASTNAME, lastname);
        args.put(KEY_USRTYPE, usertype);
        
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public void deleteAll() {
    	mDb.execSQL("DELETE FROM clouds");
    }
    
    public void execSQL(String SQL) {
    	mDb.execSQL(SQL);
    }
    
    public void createTable() {
    	execSQL(DATABASE_CREATE);
    }
    
    public void deleteDatabase() {
    	mCtx.deleteDatabase(DATABASE_NAME);
    }
}
