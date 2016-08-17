package com.shout.fragments;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.shout.applications.ShoutApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;

import java.util.HashMap;

public class PersonEventsFragment extends EventsListFragment {
    private final String PERSON_SEARCH_PHP_PATH = "http://shouttestserver.ueuo.com/person_events" +
            ".php";

    private View.OnClickListener setAddInviteListener(final EventsListFragment.EventsAdapter
            .ViewHolder holder, final String toSet) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    holder.eventInvite.put(Invite.COLUMN_INVITEE_ID, userId);
                    holder.eventInvite.put(Invite.COLUMN_TYPE, "Joined");
                    holder.eventInvite.put(Invite.COLUMN_GOING, toSet);
                    holder.eventInvite.put(Invite.COLUMN_SENT, "Yes");
                    JSONObject jsonObject = new JSONObject(holder.eventInvite);
                    new UpdateEventsView().execute(new Pair<>(UPDATE_GOING_PHP_PATH, new Pair<>
                            (holder, jsonObject)));
            }
        };
    }

    @Override
    public void setYesOnClickListener(Button button, final EventsAdapter.ViewHolder holder) {
        HashMap<String, String> eventInvite = holder.eventInvite;
        if (eventInvite.get(Invite.COLUMN_INVITEE_ID) != null) {
            button.setOnClickListener(setUpdateInviteListener(holder, "Yes"));
        } else {
            button.setOnClickListener(setAddInviteListener(holder, "Yes"));
        }
    }

    @Override
    public void setNoOnClickListener(Button button, EventsAdapter.ViewHolder holder) {
        HashMap<String, String> eventInvite = holder.eventInvite;
        if (eventInvite.get(Invite.COLUMN_INVITEE_ID) != null) {
            button.setOnClickListener(setUpdateInviteListener(holder, "No"));
        } else {
            button.setOnClickListener(setAddInviteListener(holder, "No"));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", userId);
            jsonObject.put("person_id", getArguments().getString("personId"));
            jsonObject.put("offset", "0");
            new PersonSearchTask().execute(new Pair<>(PERSON_SEARCH_PHP_PATH, new Pair<>((Void)
                    null, jsonObject)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class PersonSearchTask extends ShoutApplication.SendAndReceiveJSON<Void> {
        @Override
        protected void onPostExecute(Pair<Void, JSONObject> pair) {
            try {
                if (pair.second.get("search_results").equals("Success!")) {
                    JSONArray events = pair.second.getJSONArray("events");
                    ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(events);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}