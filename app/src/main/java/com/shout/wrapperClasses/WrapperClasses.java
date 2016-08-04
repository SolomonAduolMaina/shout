package com.shout.wrapperClasses;

import android.database.Cursor;

import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;

import java.util.ArrayList;

public class WrapperClasses {
    public static class EventDetails {
        public String inviteeId, eventId, type, going, sent, creator_id, title, location,
                description, startDateTime, endDateTime, tag, shout;

        public EventDetails(Cursor cursor) {
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

    public static class EventDetailClasses {
        public ArrayList<EventDetails> all;
        public ArrayList<EventDetails> myEvents;
        public ArrayList<EventDetails> invited;
        public ArrayList<EventDetails> suggested;

        public EventDetailClasses(Cursor cursor, String userId) {
            all = new ArrayList<>();
            myEvents = new ArrayList<>();
            invited = new ArrayList<>();
            suggested = new ArrayList<>();

            while (cursor.moveToNext()) {
                EventDetails notification = (new EventDetails(cursor));
                if (notification.creator_id.equals(userId)) {
                    myEvents.add(notification);
                }
                if (notification.inviteeId != null) {
                    if (notification.inviteeId.equals(userId)) {
                        if (notification.type.equals("Invite")) {
                            invited.add(notification);
                        } else {
                            suggested.add(notification);
                        }
                    }
                }
                all.add(notification);
            }
            cursor.close();
        }
    }
}