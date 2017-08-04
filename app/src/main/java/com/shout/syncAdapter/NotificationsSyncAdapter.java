package com.shout.syncAdapter;

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

import com.shout.R;
import com.shout.activities.ShoutActivity;
import com.shout.database.NotificationsProvider;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;
import com.shout.networkmessaging.SendMessages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NotificationsSyncAdapter extends AbstractThreadedSyncAdapter {

    public NotificationsSyncAdapter(Context context, boolean autoInitialize, boolean
            allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    private void syncData(final Pair<ContentProviderClient, JSONObject> pair) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) {
                try {
                    JSONArray events = response.getJSONArray("events");
                    JSONArray invites = response.getJSONArray("invites");

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
                        pair.first.update(NotificationsProvider.EVENT_URI, values, "", new String[]{});
                    }

                    for (int index = 0; index < invites.length(); index++) {
                        JSONObject invite = invites.getJSONObject(index);
                        ContentValues values = new ContentValues();
                        values.put(Invite.COLUMN_INVITEE_ID, invite.getString(Invite.COLUMN_INVITEE_ID));
                        values.put(Invite.COLUMN_EVENT_ID, invite.getString(Invite.COLUMN_EVENT_ID));
                        values.put(Invite.COLUMN_TYPE, invite.getString(Invite.COLUMN_TYPE));
                        values.put(Invite.COLUMN_GOING, invite.getString(Invite.COLUMN_GOING));
                        values.put(Invite.COLUMN_SENT, invite.getString(Invite.COLUMN_SENT));
                        pair.first.update(NotificationsProvider.INVITE_URI, values, "", new String[]{});
                    }

                    getContext().sendBroadcast(new Intent(ShoutActivity.ACTION_FINISHED_SYNC));
                } catch (JSONException | RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
        SendMessages.doOnResponse(lambda, getContext(), pair.second,
                getContext().getString(R.string.sync_php_path));
    }


    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient client, SyncResult syncResult) {
        android.os.Debug.waitForDebugger();
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", bundle.getString("user_id"));
            syncData(new Pair<>(client, json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}