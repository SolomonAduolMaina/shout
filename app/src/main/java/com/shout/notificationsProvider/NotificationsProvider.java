package com.shout.notificationsProvider;

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

import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;


import com.shout.R;

public class NotificationsProvider extends ContentProvider {
    public static final String AUTHORITY = "com.shout.notificationsProvider";
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

    static {
        uriMatcher.addURI(AUTHORITY, "Notifications", NOTIFICATIONS_CONSTANT);
        uriMatcher.addURI(AUTHORITY, Invite.INVITE, INVITE_CONSTANT);
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
        Cursor cursor;
        String SQL;
        switch (uriMatcher.match(uri)) {
            case NOTIFICATIONS_CONSTANT:
                SQL = "Event LEFT OUTER JOIN Invite on (Invite.event_id = Event.event_id)";
                queryBuilder.setTables(SQL);
                String[] s = new String[]{Invite.INVITE + ".*", Event.COLUMN_CREATOR_ID, Event
                        .COLUMN_TITLE, Event.COLUMN_LOCATION, Event.COLUMN_DESCRIPTION, Event
                        .COLUMN_START_DATETIME, Event.COLUMN_END_DATETIME, Event.COLUMN_TAG,
                        Event.COLUMN_SHOUT};
                cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                        s, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case EVENT_CONSTANT | ONE_EVENT_CONSTANT:
                queryBuilder.setTables("Event");
                cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                        projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
        }

        /*switch (uriMatcher.match(uri)) {
            case ONE_NOTIFICATION: // eventInvite with specified id will be selected
                queryBuilder.appendWhere(ShoutDatabaseDescription.Invite._ID + "=" + uri
                        .getLastPathSegment());
                break;
            case NOTIFICATIONS_CONSTANT: // all notifications will be selected
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string
                        .invalid_query_uri) + uri);
        }

        // execute the query to select one or all notifications
        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);

        // configure to watch for content changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;*/
        return null;
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
                long rowId = db.insertWithOnConflict(Invite.INVITE, null, contentValues,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (rowId > 0) {
                    result = ContentUris.withAppendedId(INVITE_URI, rowId);
                    resolver.notifyChange(uri, null);
                } else
                    throw new SQLException(INSERT_FAILED + uri);
                break;
            case EVENT_CONSTANT:

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

        /*int numberOfRowsDeleted;

        switch (uriMatcher.match(uri)) {
            case ONE_NOTIFICATION:
                // get from the uri the id of contact to update
                String id = uri.getLastPathSegment();

                // delete the contact
                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(Invite.INVITE_CONSTANT,
                        Invite._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_delete_uri) + uri);
        }

        // notify observers that the database changed
        if (numberOfRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsDeleted;*/
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[]
            selectionArgs) {
        int numberOfRowsUpdated;

        switch (uriMatcher.match(uri)) {
            case INVITE_CONSTANT:
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(Invite.INVITE,
                        contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_update_uri) + uri);
        }

        if (numberOfRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numberOfRowsUpdated;
    }
}