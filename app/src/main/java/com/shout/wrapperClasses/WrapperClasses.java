package com.shout.wrapperClasses;

import android.database.Cursor;

import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WrapperClasses {
    public static class EventInvite {
        public String inviteeId, eventId, type, going, sent, creator_id, title, location,
                description, startDateTime, endDateTime, tag, shout;

        public EventInvite(Cursor cursor) {
            this.inviteeId = cursor.getString(cursor.getColumnIndex(Invite.COLUMN_INVITEE_ID));
            this.eventId = cursor.getString(cursor.getColumnIndex(Invite.COLUMN_EVENT_ID));
            this.type = cursor.getString(cursor.getColumnIndex(Invite.COLUMN_TYPE));
            this.going = cursor.getString(cursor.getColumnIndex(Invite.COLUMN_GOING));
            this.sent = cursor.getString(cursor.getColumnIndex(Invite.COLUMN_SENT));
            this.creator_id = cursor.getString(cursor.getColumnIndex(Event.COLUMN_CREATOR_ID));
            this.title = cursor.getString(cursor.getColumnIndex(Event.COLUMN_TITLE));
            this.location = cursor.getString(cursor.getColumnIndex(Event.COLUMN_LOCATION));
            this.description = cursor.getString(cursor.getColumnIndex(Event.COLUMN_DESCRIPTION));
            this.startDateTime = cursor.getString(cursor.getColumnIndex(Event
                    .COLUMN_START_DATETIME));
            this.endDateTime = cursor.getString(cursor.getColumnIndex(Event.COLUMN_END_DATETIME));
            this.tag = cursor.getString(cursor.getColumnIndex(Event.COLUMN_TAG));
            this.shout = cursor.getString(cursor.getColumnIndex(Event.COLUMN_SHOUT));
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

    public static class EventInviteClasses {
        public ArrayList<EventInvite> all;
        public ArrayList<EventInvite> myEvents;
        public ArrayList<EventInvite> invited;
        public ArrayList<EventInvite> suggested;

        public EventInviteClasses(Cursor cursor, String userId) {
            all = new ArrayList<>();
            myEvents = new ArrayList<>();
            invited = new ArrayList<>();
            suggested = new ArrayList<>();

            while (cursor.moveToNext()) {
                EventInvite eventInvite = (new EventInvite(cursor));
                if (eventInvite.creator_id.equals(userId)) {
                    myEvents.add(eventInvite);
                }
                if (eventInvite.inviteeId != null) {
                    if (eventInvite.inviteeId.equals(userId)) {
                        if (eventInvite.type.equals("Invite")) {
                            invited.add(eventInvite);
                        } else {
                            suggested.add(eventInvite);
                        }
                    }
                }
                all.add(eventInvite);
            }
            cursor.close();
        }
    }
}