package com.shout.notificationsProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;


public class ShoutDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Shout.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_INVITE_TABLE =
            "CREATE TABLE " +
                    Invite.INVITE + "(" +
                    Invite.COLUMN_INVITEE_ID + " VARCHAR, " +
                    Invite.COLUMN_EVENT_ID + " VARCHAR, " +
                    Invite.COLUMN_TYPE + " VARCHAR, " +
                    Invite.COLUMN_GOING + " VARCHAR, " +
                    Invite.COLUMN_SENT + " VARCHAR, " +
                    "PRIMARY KEY(" + Invite.COLUMN_INVITEE_ID + ", " + Invite.COLUMN_EVENT_ID +
                    "));";

    private static final String CREATE_EVENT_TABLE =
            "CREATE TABLE " +
                    Event.EVENT + "(" +
                    Event.COLUMN_EVENT_ID + " VARCHAR, " +
                    Event.COLUMN_CREATOR_ID + " VARCHAR, " +
                    Event.COLUMN_TITLE + " VARCHAR, " +
                    Event.COLUMN_LOCATION + " VARCHAR, " +
                    Event.COLUMN_DESCRIPTION + " VARCHAR, " +
                    Event.COLUMN_START_DATETIME + " VARCHAR, " +
                    Event.COLUMN_END_DATETIME + " VARCHAR, " +
                    Event.COLUMN_TAG + " VARCHAR, " +
                    Event.COLUMN_SHOUT + " VARCHAR, " +
                    "PRIMARY KEY (" + Event.COLUMN_EVENT_ID + "));";

    private static final String DELETE_INVITE_TABLE = "DROP TABLE IF EXISTS " + Invite.INVITE;
    private static final String DELETE_EVENT_TABLE = "DROP TABLE IF EXISTS " + Event.EVENT;


    public ShoutDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_INVITE_TABLE);
        db.execSQL(CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_INVITE_TABLE);
        db.execSQL(DELETE_EVENT_TABLE);
        onCreate(db);
    }
}
