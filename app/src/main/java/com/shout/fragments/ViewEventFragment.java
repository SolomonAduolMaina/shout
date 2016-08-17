package com.shout.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shout.applications.ShoutApplication;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;

import org.json.JSONException;

public class ViewEventFragment extends SingleEventFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        eventInvite = ShoutApplication.BundleToHashMap(getArguments());
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        eventTitle_editText.setText(eventInvite.get(Event.COLUMN_TITLE));
        eventLocation_editText.setText(eventInvite.get(Event.COLUMN_LOCATION));
        eventDescription_editText.setText(eventInvite.get(Event.COLUMN_DESCRIPTION));

        String startDateTime = eventInvite.get(Event.COLUMN_START_DATETIME).replace("-", ":");
        int space = startDateTime.indexOf(" ");
        chooseStartDate.setText(startDateTime.substring(0, space));
        chooseStartTime.setText(startDateTime.substring(space + 1 , startDateTime.length()));

        String endDateTime = eventInvite.get(Event.COLUMN_END_DATETIME).replace("-", ":");
        space = endDateTime.indexOf(" ");
        chooseEndDate.setText(endDateTime.substring(0, space));
        chooseEndTime.setText(endDateTime.substring(space + 1 , endDateTime.length()));

        eventTag_editText.setText(eventInvite.get(Event.COLUMN_TAG));
        shoutEvent_checkBox.setChecked(Boolean.getBoolean(eventInvite.get(Event.COLUMN_SHOUT)));
        return rootView;
    }

    @Override
    public void configureButton() throws JSONException {
        final String userId = getActivity().getIntent().getStringExtra("userId");
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