package com.shout.syncAdapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Pair;

import com.shout.activities.ShoutActivity;
import com.shout.applications.ShoutApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shout.notificationsProvider.NotificationsProvider;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;


public class NotificationsSyncAdapter extends AbstractThreadedSyncAdapter {
    private final String SYNC_DATA_PHP = "http://shouttestserver.ueuo.com/sync.php";

    public NotificationsSyncAdapter(Context context, boolean autoInitialize, boolean
            allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient client, SyncResult syncResult) {
        JSONObject json = new JSONObject();
        try {
            json.put("userId", bundle.getString("userId"));
            JSONObject records = ShoutApplication.getJsonResponse(new Pair<>(SYNC_DATA_PHP, json));
            JSONArray events = records.getJSONArray("events");
            JSONArray invites = records.getJSONArray("invites");

            for (int index = 0; index < events.length(); index++) {
                JSONObject event = events.getJSONObject(index);
                ContentValues values = new ContentValues();
                values.put(Event.COLUMN_EVENT_ID, event.getString(Event.COLUMN_EVENT_ID));
                values.put(Event.COLUMN_CREATOR_ID, event.getString(Event.COLUMN_CREATOR_ID));
                values.put(Event.COLUMN_TITLE, event.getString(Event.COLUMN_TITLE));
                values.put(Event.COLUMN_LOCATION, event.getString(Event.COLUMN_LOCATION));
                values.put(Event.COLUMN_DESCRIPTION, event.getString(Event.COLUMN_DESCRIPTION));
                values.put(Event.COLUMN_START_DATETIME, event.getString(Event
                        .COLUMN_START_DATETIME));
                values.put(Event.COLUMN_END_DATETIME, event.getString(Event.COLUMN_END_DATETIME));
                values.put(Event.COLUMN_TAG, event.getString(Event.COLUMN_TAG));
                values.put(Event.COLUMN_SHOUT, event.getString(Event.COLUMN_SHOUT));
                client.insert(NotificationsProvider.EVENT_URI, values);
            }

            for (int index = 0; index < invites.length(); index++) {
                JSONObject invite = invites.getJSONObject(index);
                ContentValues values = new ContentValues();
                values.put(Invite.COLUMN_INVITEE_ID, invite.getString(Invite.COLUMN_INVITEE_ID));
                values.put(Invite.COLUMN_EVENT_ID, invite.getString(Invite.COLUMN_EVENT_ID));
                values.put(Invite.COLUMN_TYPE, invite.getString(Invite.COLUMN_TYPE));
                values.put(Invite.COLUMN_GOING, invite.getString(Invite.COLUMN_GOING));
                values.put(Invite.COLUMN_SENT, invite.getString(Invite.COLUMN_SENT));
                client.insert(NotificationsProvider.INVITE_URI, values);
            }

            getContext().sendBroadcast(new Intent(ShoutActivity.ACTION_FINISHED_SYNC));
        } catch (JSONException | RemoteException e) {
            e.printStackTrace();
        }
    }
}