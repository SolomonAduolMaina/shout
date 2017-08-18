package com.shout.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.shout.R;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;

public class NotificationsProvider extends ContentProvider {
    public static final String AUTHORITY = "com.shout.database.NotificationsProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri NOTIFICATIONS_URI = BASE_CONTENT_URI.buildUpon().appendPath
            ("Notifications").build();
    public static final Uri INVITE_URI = BASE_CONTENT_URI.buildUpon().appendPath("Invite").build();
    public static final Uri EVENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("Event").build();


    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int NOTIFICATIONS_CONSTANT = 0;
    private static final int INVITE_CONSTANT = 1;
    private static final int EVENT_CONSTANT = 2;
    private static final int ONE_EVENT_CONSTANT = 3;
    private static final int MY_EVENTS_CONSTANT = 4;
    private static final int ONE_INVITE_CONSTANT = 5;

    static {
        uriMatcher.addURI(AUTHORITY, "Notifications", NOTIFICATIONS_CONSTANT);
        uriMatcher.addURI(AUTHORITY, Invite.INVITE, INVITE_CONSTANT);
        uriMatcher.addURI(AUTHORITY, Invite.INVITE + "/#", ONE_INVITE_CONSTANT);
        uriMatcher.addURI(AUTHORITY, Event.EVENT, EVENT_CONSTANT);
        uriMatcher.addURI(AUTHORITY, Event.EVENT + "/#", ONE_EVENT_CONSTANT);
        uriMatcher.addURI(AUTHORITY, "MyEvents", MY_EVENTS_CONSTANT);

    }

    private String INSERT_FAILED;
    private String INVALID_INSERT_URI;

    private ShoutDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ShoutDatabaseHelper(getContext());
        INSERT_FAILED = getContext().getString(R.string.insert_failed);
        INVALID_INSERT_URI = getContext().getString(R.string.invalid_insert_uri);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor = null;
        String SQL;
        switch (uriMatcher.match(uri)) {
            case NOTIFICATIONS_CONSTANT:
                SQL = "Event LEFT OUTER JOIN Invite on Event.event_id = Invite.event_id";
                queryBuilder.setTables(SQL);
                String[] columns = new String[]{"Event.*", "Invite." + Invite.COLUMN_INVITEE_ID,
                        "Invite." + Invite.COLUMN_TYPE, "Invite." + Invite.COLUMN_GOING, "Invite" +
                        "." + Invite.COLUMN_SENT};
                cursor = queryBuilder.query(dbHelper.getReadableDatabase(), columns, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case EVENT_CONSTANT:
            case ONE_EVENT_CONSTANT:
                queryBuilder.setTables("Event");
                cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                        projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Context context = getContext();
        ContentResolver resolver = context.getContentResolver();
        Uri result;
        switch (uriMatcher.match(uri)) {
            case INVITE_CONSTANT:
            case ONE_INVITE_CONSTANT:
                long rowId = db.insertWithOnConflict(Invite.INVITE, null, contentValues,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (rowId > 0) {
                    result = ContentUris.withAppendedId(INVITE_URI, rowId);
                    resolver.notifyChange(uri, null);
                } else
                    throw new SQLException(INSERT_FAILED + uri);
                break;
            case EVENT_CONSTANT:
            case ONE_EVENT_CONSTANT:
                rowId = db.insertWithOnConflict(Event.EVENT, null, contentValues, SQLiteDatabase
                        .CONFLICT_REPLACE);
                if (rowId > 0) {
                    result = ContentUris.withAppendedId(EVENT_URI, rowId);
                    resolver.notifyChange(uri, null);
                } else
                    throw new SQLException(INSERT_FAILED + uri);
                break;
            default:
                throw new UnsupportedOperationException(INVALID_INSERT_URI + uri);
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numberOfRowsDeleted = 0;
        switch (uriMatcher.match(uri)) {
            case NOTIFICATIONS_CONSTANT:
                break;
            case EVENT_CONSTANT:
            case ONE_EVENT_CONSTANT:
                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete("Event", selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_delete_uri) + uri);
        }
        if (numberOfRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numberOfRowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[]
            selectionArgs) {
        int numberOfRowsUpdated;
        switch (uriMatcher.match(uri)) {
            case INVITE_CONSTANT:
            case ONE_INVITE_CONSTANT:
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(Invite.INVITE,
                        contentValues, selection, selectionArgs);
                if (numberOfRowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Uri insert = insert(uri, contentValues);
                    numberOfRowsUpdated = update(insert, contentValues, selection, selectionArgs);
                }
                break;
            case EVENT_CONSTANT:
            case ONE_EVENT_CONSTANT:
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(Event.EVENT,
                        contentValues, selection, selectionArgs);
                if (numberOfRowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Uri insert = insert(uri, contentValues);
                    numberOfRowsUpdated = update(insert, contentValues, selection, selectionArgs);
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_update_uri) + uri);
        }
        return numberOfRowsUpdated;
    }
}