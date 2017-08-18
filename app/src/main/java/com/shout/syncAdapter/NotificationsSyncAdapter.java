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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

import com.shout.R;
import com.shout.database.NotificationsProvider;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;
import com.shout.fragments.MyEventsFragment;
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
            public void process(JSONObject response) throws JSONException {
                try {
                    if (response.get("result").equals("Success!")) {
                        JSONArray events = response.getJSONArray("events");
                        JSONArray invites = response.getJSONArray("invites");

                        for (int index = 0; index < events.length(); index++) {
                            JSONObject event = events.getJSONObject(index);
                            ContentValues values = new ContentValues();
                            for (String column : Event.COLUMNS) {
                                values.put(column, event.getString(column));
                            }
                            pair.first.update(NotificationsProvider.EVENT_URI, values, "", new String[]{});
                        }

                        for (int index = 0; index < invites.length(); index++) {
                            JSONObject invite = invites.getJSONObject(index);
                            ContentValues values = new ContentValues();
                            for (String column : Invite.COLUMNS) {
                                values.put(column, invite.getString(column));
                            }
                            pair.first.update(NotificationsProvider.INVITE_URI, values, "", new String[]{});
                        }
                    }
                } catch (RemoteException e) {
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
            Intent intent = new Intent(MyEventsFragment.ACTION_UPDATED_DATABASE);
            intent.putExtra("notification", bundle.getString("notification"));
            intent.putExtra("event_id", bundle.getString("event_id"));
            intent.putExtra("type", bundle.getString("type"));
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}