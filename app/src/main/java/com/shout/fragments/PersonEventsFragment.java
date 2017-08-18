package com.shout.fragments;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shout.R;
import com.shout.networkmessaging.SendMessages;

import org.json.JSONException;
import org.json.JSONObject;

public class PersonEventsFragment extends EventsListFragment {
    public static final String PERSON_EVENTS_FRAGMENT = "PERSON_EVENTS_FRAGMENT";
    private int OFFSET = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        String userId = getActivity().getIntent().getStringExtra("user_id");
        String personId = getArguments().getString("person_id");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", userId);
            jsonObject.put("person_id", personId);
            jsonObject.put("offset", OFFSET);
            getPersonEventsTask(new Pair<>(personId, jsonObject));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    void getPersonEventsTask(final Pair<String, JSONObject> pair) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) throws JSONException {
                if (response.get("result").equals("Success!")) {
                    ((EventsAdapter) eventsRecyclerView.getAdapter()).
                            setData(response.getJSONArray("events"), pair.first);
                }
            }
        };
        SendMessages.doOnResponse(lambda, getActivity(), pair.second,
                getString(R.string.person_events_path));
    }
}