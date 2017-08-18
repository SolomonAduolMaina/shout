// DatabaseDescription.java
// Describes the table name and column names for this app's database,
// and other information required by the ContentProvider
package com.shout.database;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Arrays;

public class ShoutDatabaseDescription {
    public static final class Invite implements BaseColumns {
        public static final String INVITE = "Invite";
        public static final String COLUMN_INVITEE_ID = "invitee_id";
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_GOING = "going";
        public static final String COLUMN_SENT = "sent";

        public static ArrayList<String> COLUMNS = new ArrayList<>(
                Arrays.asList(
                        COLUMN_INVITEE_ID,
                        COLUMN_EVENT_ID,
                        COLUMN_TYPE,
                        COLUMN_GOING,
                        COLUMN_SENT));
    }

    public static final class Event implements BaseColumns {
        public static final String EVENT = "Event";
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_CREATOR_ID = "creator_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_SHOUT = "shout";
        public static final String COLUMN_LOCATION_NAME = "location_name";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_START_YEAR = "start_year";
        public static final String COLUMN_START_MONTH = "start_month";
        public static final String COLUMN_START_DAY = "start_day";
        public static final String COLUMN_START_HOUR = "start_hour";
        public static final String COLUMN_START_MINUTE = "start_minute";
        public static final String COLUMN_END_YEAR = "end_year";
        public static final String COLUMN_END_MONTH = "end_month";
        public static final String COLUMN_END_DAY = "end_day";
        public static final String COLUMN_END_HOUR = "end_hour";
        public static final String COLUMN_END_MINUTE = "end_minute";

        public static ArrayList<String> COLUMNS = new ArrayList<>(
                Arrays.asList(
                        COLUMN_EVENT_ID,
                        COLUMN_CREATOR_ID,
                        COLUMN_TITLE,
                        COLUMN_DESCRIPTION,
                        COLUMN_TAG,
                        COLUMN_SHOUT,
                        COLUMN_LOCATION_NAME,
                        COLUMN_LATITUDE,
                        COLUMN_LONGITUDE,
                        COLUMN_START_YEAR,
                        COLUMN_START_MONTH,
                        COLUMN_START_DAY,
                        COLUMN_START_HOUR,
                        COLUMN_START_MINUTE,
                        COLUMN_END_YEAR,
                        COLUMN_END_MONTH,
                        COLUMN_END_DAY,
                        COLUMN_END_HOUR,
                        COLUMN_END_MINUTE)
        );

    }
}