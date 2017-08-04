package com.shout.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import com.shout.utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseUtilities {
    public static Pair<Integer, Integer> updateEventInvite(HashMap<String, String> eventInvite,
                                                           Context context, boolean addInvite) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_EVENT_ID, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_EVENT_ID));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_TITLE, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_TITLE));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_LOCATION, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_LOCATION));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_DESCRIPTION, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_DESCRIPTION));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_START_DATETIME, eventInvite.get(ShoutDatabaseDescription.Event
                .COLUMN_START_DATETIME));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_END_DATETIME, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_END_DATETIME));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_TAG, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_TAG));
        contentValues.put(ShoutDatabaseDescription.Event.COLUMN_SHOUT, eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_SHOUT));
        int eventRows = context.getContentResolver().update(NotificationsProvider.EVENT_URI,
                contentValues, ShoutDatabaseDescription.Event.COLUMN_EVENT_ID + "= ?", new String[]{eventInvite.get(ShoutDatabaseDescription.Event
                        .COLUMN_EVENT_ID)});
        int inviteRows = 0;
        if (addInvite) {
            contentValues = new ContentValues();
            contentValues.put(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID, eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID));
            contentValues.put(ShoutDatabaseDescription.Invite.COLUMN_EVENT_ID, eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_EVENT_ID));
            contentValues.put(ShoutDatabaseDescription.Invite.COLUMN_TYPE, eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_TYPE));
            contentValues.put(ShoutDatabaseDescription.Invite.COLUMN_GOING, eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_GOING));
            contentValues.put(ShoutDatabaseDescription.Invite.COLUMN_SENT, eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_SENT));
            inviteRows = context.getContentResolver().update(NotificationsProvider.INVITE_URI,
                    contentValues, ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID + "= ? AND " + ShoutDatabaseDescription.Invite.COLUMN_EVENT_ID +
                            " = ?", new String[]{eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID),
                            eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_EVENT_ID)});
        } else {

        }
        return new Pair<>(eventRows, inviteRows);
    }

    public static HashMap<String, String> eventInvite(Cursor cursor) {
        HashMap<String, String> eventInvite = new HashMap<>();
        eventInvite.put(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Invite
                .COLUMN_INVITEE_ID)));
        eventInvite.put(ShoutDatabaseDescription.Invite.COLUMN_EVENT_ID, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Invite
                .COLUMN_EVENT_ID)));
        eventInvite.put(ShoutDatabaseDescription.Invite.COLUMN_TYPE, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Invite
                .COLUMN_TYPE)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event
                .COLUMN_CREATOR_ID)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event
                .COLUMN_TITLE)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_LOCATION, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event
                .COLUMN_LOCATION)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_DESCRIPTION, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event
                .COLUMN_DESCRIPTION)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_START_DATETIME, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event
                .COLUMN_START_DATETIME)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_END_DATETIME, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event
                .COLUMN_END_DATETIME)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_TAG, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event.COLUMN_TAG)));
        eventInvite.put(ShoutDatabaseDescription.Event.COLUMN_SHOUT, cursor.getString(cursor.getColumnIndex(ShoutDatabaseDescription.Event
                .COLUMN_SHOUT)));
        return eventInvite;
    }

    public static class EventInviteClasses {
        public ArrayList<HashMap<String, String>> all;
        public ArrayList<HashMap<String, String>> myEvents;
        public ArrayList<HashMap<String, String>> invited;
        public ArrayList<HashMap<String, String>> suggested;

        public EventInviteClasses(JSONArray data) throws JSONException {
            all = new ArrayList<>();
            myEvents = new ArrayList<>();
            invited = new ArrayList<>();
            suggested = new ArrayList<>();

            for (int index = 0; index < data.length(); index++) {
                HashMap<String, String> eventInvite = Util.JSONObjectToHashMap(data.getJSONObject
                        (index));
                if (eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID) != null) {
                    if (eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_TYPE).equals("Invite")) {
                        invited.add(eventInvite);
                    } else {
                        suggested.add(eventInvite);
                    }
                } else {
                    myEvents.add(eventInvite);
                }
                all.add(eventInvite);
            }
        }

        public EventInviteClasses(Cursor cursor, String userId) {
            all = new ArrayList<>();
            myEvents = new ArrayList<>();
            invited = new ArrayList<>();
            suggested = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HashMap<String, String> eventInvite = DatabaseUtilities.eventInvite(cursor);
                    if (eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID).equals(userId)) {
                        myEvents.add(eventInvite);
                    }
                    if (eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID) != null) {
                        if (eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID).equals(userId)) {
                            if (eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_TYPE).equals("Invite")) {
                                invited.add(eventInvite);
                            } else {
                                suggested.add(eventInvite);
                            }
                        }
                    }
                    all.add(eventInvite);
                }
            }
        }
    }

    public static class SearchClasses {
        public JSONArray eventInvites;
        public JSONArray persons;
        public JSONArray groups;

        public SearchClasses(JSONObject jsonObject) throws JSONException {
            eventInvites = jsonObject.getJSONArray("events");
            persons = jsonObject.getJSONArray("friends");
            groups = jsonObject.getJSONArray("groups");
        }
    }
}
