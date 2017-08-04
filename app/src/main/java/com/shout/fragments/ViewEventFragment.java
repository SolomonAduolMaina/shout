package com.shout.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;
import com.shout.utilities.Util;

import org.json.JSONException;

public class ViewEventFragment extends SingleEventFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        eventInvite = Util.BundleToHashMap(getArguments());
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        eventTitle_editText.setText(eventInvite.get(Event.COLUMN_TITLE));
        location_autoComplete.setText(eventInvite.get(Event.COLUMN_LOCATION));
        eventDescription_editText.setText(eventInvite.get(Event.COLUMN_DESCRIPTION));

        String startDateTime = eventInvite.get(Event.COLUMN_START_DATETIME);
        int firstColon = startDateTime.indexOf(":");
        String afterFirstColon = startDateTime.substring(firstColon + 1, startDateTime.length());
        int secondColon = afterFirstColon.indexOf(":");
        int desiredColon = firstColon + secondColon + 1;
        chooseStartDate.setText(startDateTime.substring(0, desiredColon));
        chooseStartTime.setText(startDateTime.substring(desiredColon + 1, startDateTime.length() - 1));

        String endDateTime = eventInvite.get(Event.COLUMN_END_DATETIME);
        firstColon = endDateTime.indexOf(":");
        afterFirstColon = endDateTime.substring(firstColon + 1, endDateTime.length());
        secondColon = afterFirstColon.indexOf(":");
        desiredColon = firstColon + secondColon + 1;
        chooseEndDate.setText(endDateTime.substring(0, desiredColon));
        chooseEndTime.setText(endDateTime.substring(desiredColon + 1, endDateTime.length() - 1));

        eventTag_editText.setText(eventInvite.get(Event.COLUMN_TAG));
        shoutEvent_checkBox.setChecked(eventInvite.get(Event.COLUMN_SHOUT).equals("true"));
        return rootView;
    }

    @Override
    public void configureButton() throws JSONException {
        final String userId = getActivity().getIntent().getStringExtra("user_id");
        if (eventInvite.get(Event.COLUMN_CREATOR_ID).equals(userId)) {
            createEvent_Button.setText("Confirm Changes");
        } else if (eventInvite.get(Invite.COLUMN_INVITEE_ID) != null) {
            if (eventInvite.get(Invite.COLUMN_INVITEE_ID).equals(userId)) {
                if (eventInvite.get(Invite.COLUMN_GOING).equals("Yes")) {
                    createEvent_Button.setText("Unfollow event");
                } else {
                    createEvent_Button.setText("Accept invite");
                }
            }
        } else {
            createEvent_Button.setText("Join event");
        }
        createEvent_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createEvent_Button.getText().toString().equals("Confirm Changes")) {
                    updateEvent(false, false);
                } else {
                    if (createEvent_Button.getText().toString().equals("Accept invite")) {
                        eventInvite.put(Invite.COLUMN_GOING, "Yes");
                    } else if (createEvent_Button.getText().toString().equals("Unfollow event")) {
                        eventInvite.put(Invite.COLUMN_GOING, "No");
                    } else if (createEvent_Button.getText().toString().equals("Join event")) {
                        eventInvite.put(Invite.COLUMN_INVITEE_ID, userId);
                        eventInvite.put(Invite.COLUMN_GOING, "Yes");
                        eventInvite.put(Invite.COLUMN_SENT, "Yes");
                        eventInvite.put(Invite.COLUMN_TYPE, "Joined");
                    }
                    updateEvent(false, true);
                }
            }
        });
    }
}