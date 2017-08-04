// DatabaseDescription.java
// Describes the table name and column names for this app's database,
// and other information required by the ContentProvider
package com.shout.database;

import android.provider.BaseColumns;

public class ShoutDatabaseDescription {
    public static final class Invite implements BaseColumns {
        public static final String INVITE = "Invite";
        public static final String COLUMN_INVITEE_ID = "invitee_id";
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_GOING = "going";
        public static final String COLUMN_SENT = "sent";
    }

    public static final class Event implements BaseColumns {
        public static final String EVENT = "Event";
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_CREATOR_ID = "creator_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_START_DATETIME = "start_datetime";
        public static final String COLUMN_END_DATETIME = "end_datetime";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_SHOUT = "shout";
    }
}