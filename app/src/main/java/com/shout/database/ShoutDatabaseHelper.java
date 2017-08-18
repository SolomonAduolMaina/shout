package com.shout.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;


class ShoutDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Shout.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_INVITE_TABLE =
            "CREATE TABLE " + Invite.INVITE + "(" + inviteColumns() + "PRIMARY KEY("
                    + Invite.COLUMN_INVITEE_ID + ", " + Invite.COLUMN_EVENT_ID + "));";
    private static final String CREATE_EVENT_TABLE =
            "CREATE TABLE " + Event.EVENT + "(" + eventColumns() +
                    "PRIMARY KEY (" + Event.COLUMN_EVENT_ID + "));";
    private static final String DELETE_INVITE_TABLE = "DROP TABLE IF EXISTS " + Invite.INVITE;
    private static final String DELETE_EVENT_TABLE = "DROP TABLE IF EXISTS " + Event.EVENT;

    ShoutDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static String inviteColumns() {
        String temp = "";
        for (String column : ShoutDatabaseDescription.Invite.COLUMNS) {
            temp = temp + column + " VARCHAR, ";
        }
        return temp;
    }

    private static String eventColumns() {
        String temp = "";
        for (String column : ShoutDatabaseDescription.Event.COLUMNS) {
            temp = temp + column + " VARCHAR, ";
        }
        return temp;
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
