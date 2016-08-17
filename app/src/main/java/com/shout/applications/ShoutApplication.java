package com.shout.applications;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.shout.notificationsProvider.NotificationsProvider;
import com.shout.notificationsProvider.ShoutDatabaseDescription;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ShoutApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Facebook SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }


    public static String readInputStream(InputStream inputStream) {
        if (inputStream != null) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    public static Bundle HashMapToBundle(HashMap<String, String> hashMap) {
        Bundle result = new Bundle();
        for (String key : hashMap.keySet()) {
            result.putString(key, hashMap.get(key));
        }
        return result;
    }

    public static HashMap<String, String> BundleToHashMap(Bundle bundle) {
        HashMap<String, String> jsonObject = new HashMap<>();
        for (String key : bundle.keySet()) {
            jsonObject.put(key, bundle.getString(key));
        }
        return jsonObject;
    }

    public static HashMap<String, String> JSONObjectToHashMap(JSONObject jsonObject) throws
            JSONException {
        Iterator<String> keys = jsonObject.keys();
        HashMap<String, String> map = new HashMap<>();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonObject.getString(key));
        }
        return map;
    }

    public static HashMap<String, String> eventInvite(Cursor cursor) {
        HashMap<String, String> eventInvite = new HashMap<>();
        eventInvite.put(Invite.COLUMN_INVITEE_ID, cursor.getString(cursor.getColumnIndex(Invite
                .COLUMN_INVITEE_ID)));
        eventInvite.put(Invite.COLUMN_EVENT_ID, cursor.getString(cursor.getColumnIndex(Invite
                .COLUMN_EVENT_ID)));
        eventInvite.put(Invite.COLUMN_TYPE, cursor.getString(cursor.getColumnIndex(Invite
                .COLUMN_TYPE)));
        eventInvite.put(Event.COLUMN_CREATOR_ID, cursor.getString(cursor.getColumnIndex(Event
                .COLUMN_CREATOR_ID)));
        eventInvite.put(Event.COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(Event
                .COLUMN_TITLE)));
        eventInvite.put(Event.COLUMN_LOCATION, cursor.getString(cursor.getColumnIndex(Event
                .COLUMN_LOCATION)));
        eventInvite.put(Event.COLUMN_DESCRIPTION, cursor.getString(cursor.getColumnIndex(Event
                .COLUMN_DESCRIPTION)));
        eventInvite.put(Event.COLUMN_START_DATETIME, cursor.getString(cursor.getColumnIndex(Event
                .COLUMN_START_DATETIME)));
        eventInvite.put(Event.COLUMN_END_DATETIME, cursor.getString(cursor.getColumnIndex(Event
                .COLUMN_END_DATETIME)));
        eventInvite.put(Event.COLUMN_TAG, cursor.getString(cursor.getColumnIndex(Event.COLUMN_TAG)));
        eventInvite.put(Event.COLUMN_SHOUT, cursor.getString(cursor.getColumnIndex(Event
                .COLUMN_SHOUT)));
        return eventInvite;
    }

    public static class SearchClasses {
        public JSONArray eventInvites;
        public JSONArray persons;
        public JSONArray groups;

        public SearchClasses(JSONObject jsonObject) throws JSONException{
            eventInvites = jsonObject.getJSONArray("events");
            persons = jsonObject.getJSONArray("friends");
            groups = jsonObject.getJSONArray("groups");
        }
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
                HashMap<String, String> eventInvite = JSONObjectToHashMap(data.getJSONObject
                        (index));
                if (eventInvite.get(Invite.COLUMN_INVITEE_ID) != null) {
                    if (eventInvite.get(Invite.COLUMN_TYPE).equals("Invite")) {
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
                    HashMap<String, String> eventInvite = eventInvite(cursor);
                    if (eventInvite.get(Event.COLUMN_CREATOR_ID).equals(userId)) {
                        myEvents.add(eventInvite);
                    }
                    if (eventInvite.get(Invite.COLUMN_INVITEE_ID) != null) {
                        if (eventInvite.get(Invite.COLUMN_INVITEE_ID).equals(userId)) {
                            if (eventInvite.get(Invite.COLUMN_TYPE).equals("Invite")) {
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

    public static Pair<Integer, Integer> updateEventInvite(HashMap<String, String> eventInvite,
                                                           Context context, boolean addInvite) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Event.COLUMN_EVENT_ID, eventInvite.get(Event.COLUMN_EVENT_ID));
        contentValues.put(Event.COLUMN_CREATOR_ID, eventInvite.get(Event.COLUMN_CREATOR_ID));
        contentValues.put(Event.COLUMN_TITLE, eventInvite.get(Event.COLUMN_TITLE));
        contentValues.put(Event.COLUMN_LOCATION, eventInvite.get(Event.COLUMN_LOCATION));
        contentValues.put(Event.COLUMN_DESCRIPTION, eventInvite.get(Event.COLUMN_DESCRIPTION));
        contentValues.put(Event.COLUMN_START_DATETIME, eventInvite.get(Event
                .COLUMN_START_DATETIME));
        contentValues.put(Event.COLUMN_END_DATETIME, eventInvite.get(Event.COLUMN_END_DATETIME));
        contentValues.put(Event.COLUMN_TAG, eventInvite.get(Event.COLUMN_TAG));
        contentValues.put(Event.COLUMN_SHOUT, eventInvite.get(Event.COLUMN_SHOUT));
        int eventRows = context.getContentResolver().update(NotificationsProvider.EVENT_URI,
                contentValues, Event.COLUMN_EVENT_ID + "= ?", new String[]{eventInvite.get(Event
                        .COLUMN_EVENT_ID)});
        int inviteRows = 0;
        if (addInvite) {
            contentValues = new ContentValues();
            contentValues.put(Invite.COLUMN_INVITEE_ID, eventInvite.get(Invite.COLUMN_INVITEE_ID));
            contentValues.put(Invite.COLUMN_EVENT_ID, eventInvite.get(Invite.COLUMN_EVENT_ID));
            contentValues.put(Invite.COLUMN_TYPE, eventInvite.get(Invite.COLUMN_TYPE));
            contentValues.put(Invite.COLUMN_GOING, eventInvite.get(Invite.COLUMN_GOING));
            contentValues.put(Invite.COLUMN_SENT, eventInvite.get(Invite.COLUMN_SENT));
            inviteRows = context.getContentResolver().update(NotificationsProvider.INVITE_URI,
                    contentValues, Invite.COLUMN_INVITEE_ID + "= ? AND " + Invite.COLUMN_EVENT_ID +
                            " = ?", new String[]{eventInvite.get(Invite.COLUMN_INVITEE_ID),
                            eventInvite.get(Event.COLUMN_EVENT_ID)});
        }
        return new Pair<>(eventRows, inviteRows);
    }

    public static abstract class SendAndReceiveJSON<Input> extends AsyncTask<Pair<String,
            Pair<Input, JSONObject>>, Void, Pair<Input, JSONObject>> {

        @Override
        protected Pair<Input, JSONObject> doInBackground(Pair<String, Pair<Input, JSONObject>>...
                                                                 args) {
            Pair<String, JSONObject> pair = new Pair<>(args[0].first, args[0].second.second);
            return (new Pair<>(args[0].second.first, getJsonResponse(pair)));
        }

        @Override
        protected abstract void onPostExecute(Pair<Input, JSONObject> pair);
    }

    @NonNull
    public static JSONObject getJsonResponse(Pair<String, JSONObject> pair) {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        JSONObject response = new JSONObject();
        try {
            URL url = new URL(pair.first);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            String message = pair.second.toString();
            connection.setFixedLengthStreamingMode(message.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.connect();
            outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(message.getBytes());
            outputStream.flush();
            inputStream = connection.getInputStream();
            inputStream = new BufferedInputStream(connection.getInputStream());
            response = new JSONObject(readInputStream(inputStream));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}