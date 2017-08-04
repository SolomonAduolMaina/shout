package com.shout.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shout.database.ShoutDatabaseDescription.Invite;
import com.shout.networkmessaging.SendMessages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PersonEventsFragment extends EventsListFragment {
    private final String PERSON_SEARCH_PHP_PATH = "http://10.0.2.2/person_events.php";

    private final PersonEventsFragment instance = this;

    private View.OnClickListener setAddInviteListener(final EventsListFragment.EventsAdapter
            .ViewHolder holder, final String toSet) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.eventInvite.put(Invite.COLUMN_INVITEE_ID, userId);
                holder.eventInvite.put(Invite.COLUMN_TYPE, "Joined");
                holder.eventInvite.put(Invite.COLUMN_GOING, toSet);
                holder.eventInvite.put(Invite.COLUMN_SENT, "Yes");
                instance.personSearchTask(new JSONObject(holder.eventInvite));
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
            personSearchTask(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void personSearchTask(final JSONObject jsonObject) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) {
                try {
                    if (response.get("search_results").equals("Success!")) {
                        JSONArray events = response.getJSONArray("events");
                        ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(events);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        SendMessages.doOnResponse(lambda, getActivity(), jsonObject, PERSON_SEARCH_PHP_PATH);
    }
}