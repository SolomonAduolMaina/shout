package com.shout.database;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shout.R;
import com.shout.fragments.ViewEventFragment;
import com.shout.utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.shout.activities.ShoutActivity.VIEW_EVENT_FRAGMENT;

public class DatabaseUtilities {
    public static Pair<Integer, Integer> updateLocalDatabase(HashMap<String, String> eventInvite,
                                                             Context context, boolean addInvite) {
        ContentValues contentValues = new ContentValues();

        for (String column : ShoutDatabaseDescription.Event.COLUMNS) {
            contentValues.put(column, eventInvite.get(column));
        }

        int eventRows = context.getContentResolver().update(NotificationsProvider.EVENT_URI,
                contentValues, ShoutDatabaseDescription.Event.COLUMN_EVENT_ID + "= ?",
                new String[]{eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_EVENT_ID)});

        int inviteRows = 0;
        if (addInvite) {
            contentValues = new ContentValues();
            for (String column : ShoutDatabaseDescription.Invite.COLUMNS) {
                contentValues.put(column, eventInvite.get(column));
            }
            inviteRows = context.getContentResolver().update(NotificationsProvider.INVITE_URI,
                    contentValues,
                    ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID + "= ? AND " +
                            ShoutDatabaseDescription.Invite.COLUMN_EVENT_ID + " = ?",
                    new String[]{eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID),
                            eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_EVENT_ID)});
        }
        return new Pair<>(eventRows, inviteRows);
    }

    public static HashMap<String, String> eventInvite(Cursor cursor) {
        HashMap<String, String> eventInvite = new HashMap<>();
        for (String column : ShoutDatabaseDescription.Event.COLUMNS) {
            eventInvite.put(column, cursor.getString(cursor.getColumnIndex(column)));
        }
        for (String column : ShoutDatabaseDescription.Invite.COLUMNS) {
            eventInvite.put(column, cursor.getString(cursor.getColumnIndex(column)));
        }
        return eventInvite;
    }

    public static class EventInviteClasses {
        public ArrayList<HashMap<String, String>> all;
        public ArrayList<HashMap<String, String>> userEvents;
        public ArrayList<HashMap<String, String>> invited;
        public ArrayList<HashMap<String, String>> suggested;

        public EventInviteClasses(JSONArray data, String userId) throws JSONException {
            all = new ArrayList<>();
            userEvents = new ArrayList<>();
            invited = new ArrayList<>();
            suggested = new ArrayList<>();

            for (int index = 0; index < data.length(); index++) {
                HashMap<String, String> eventInvite = Util.JSONObjectToHashMap(data.getJSONObject
                        (index));
                if (eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID).equals(userId)) {
                    userEvents.add(eventInvite);
                }
                if (!eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID).equals("null")
                        || eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID) == null) {
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

        public EventInviteClasses(Cursor cursor, String userId) {
            all = new ArrayList<>();
            userEvents = new ArrayList<>();
            invited = new ArrayList<>();
            suggested = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HashMap<String, String> eventInvite = DatabaseUtilities.eventInvite(cursor);
                    if (eventInvite.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID).equals(userId)) {
                        userEvents.add(eventInvite);
                    }
                    if (!eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID).equals("null")
                            || eventInvite.get(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID) == null) {
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
        public ArrayList<HashMap<String, String>> events;
        public ArrayList<HashMap<String, String>> people;
        public ArrayList<HashMap<String, String>> groups;

        public SearchClasses(JSONObject jsonObject) throws JSONException {
            events = new ArrayList<>();
            people = new ArrayList<>();
            groups = new ArrayList<>();

            JSONArray eventsArray = jsonObject.getJSONArray("events");
            for (int index = 0; index < eventsArray.length(); index++) {
                HashMap<String, String> event = Util.JSONObjectToHashMap(eventsArray.getJSONObject
                        (index));
                events.add(index, event);
            }

            JSONArray peopleArray = jsonObject.getJSONArray("people");
            for (int index = 0; index < peopleArray.length(); index++) {
                HashMap<String, String> event = Util.JSONObjectToHashMap(peopleArray.getJSONObject
                        (index));
                people.add(index, event);
            }

            JSONArray groupsArray = jsonObject.getJSONArray("groups");
            for (int index = 0; index < groupsArray.length(); index++) {
                HashMap<String, String> event = Util.JSONObjectToHashMap(groupsArray.getJSONObject
                        (index));
                groups.add(index, event);
            }
        }
    }

    public abstract static class EventsViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public HashMap<String, String> data;
        private FragmentManager fragmentManager;

        public EventsViewHolder(View view, FragmentManager fragmentManager) {
            super(view);
            this.view = view;
            this.fragmentManager = fragmentManager;
        }

        public abstract void setYesOnClickListener(Button button);

        public abstract void setNoOnClickListener(Button button);

        public void updateData(HashMap<String, String> newData) {
            this.data = newData;
            if (newData.get("going").equals("Yes")) {
                view.findViewById(R.id.going_no).setVisibility(View.VISIBLE);
                view.findViewById(R.id.going_yes).setVisibility(View.GONE);
                ((CardView) view.findViewById(R.id.events_cardView)).setCardBackgroundColor(Color.GREEN);
            } else if (newData.get("going").equals("No")) {
                view.findViewById(R.id.going_yes).setVisibility(View.VISIBLE);
                view.findViewById(R.id.going_no).setVisibility(View.GONE);
                ((CardView) view.findViewById(R.id.events_cardView)).setCardBackgroundColor(Color.RED);
            }
        }

        public void setEventsViewHolder(int position, ArrayList<HashMap<String, String>> data) {
            if (data != null) {
                this.data = data.get(position);

                ((TextView) view.findViewById(R.id.title_textView)).setText(
                        this.data.get(ShoutDatabaseDescription.Event.COLUMN_TITLE));
                ((TextView) view.findViewById(R.id.location_textView)).setText(
                        this.data.get(ShoutDatabaseDescription.Event.COLUMN_LOCATION_NAME));

                String startDateTime =
                        this.data.get(ShoutDatabaseDescription.Event.COLUMN_START_YEAR) + ":" +
                                this.data.get(ShoutDatabaseDescription.Event.COLUMN_START_MONTH) + ":" +
                                this.data.get(ShoutDatabaseDescription.Event.COLUMN_START_DAY) + ":" +
                                this.data.get(ShoutDatabaseDescription.Event.COLUMN_START_HOUR) + ":" +
                                this.data.get(ShoutDatabaseDescription.Event.COLUMN_START_MINUTE);
                ((TextView) view.findViewById(R.id.startDateTime_textView)).setText(startDateTime);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ViewEventFragment eventFragment = new ViewEventFragment();
                        eventFragment.setArguments(Util.HashMapToBundle(EventsViewHolder.this.data));
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment, eventFragment, VIEW_EVENT_FRAGMENT);
                        transaction.addToBackStack(VIEW_EVENT_FRAGMENT);
                        transaction.commit();
                    }
                });
            }

            setYesOnClickListener((Button) view.findViewById(R.id.going_yes));
            setNoOnClickListener((Button) view.findViewById(R.id.going_no));
        }
    }
}
