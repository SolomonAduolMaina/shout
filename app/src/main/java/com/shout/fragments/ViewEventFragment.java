package com.shout.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shout.R;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;
import com.shout.utilities.Util;

public class ViewEventFragment extends SingleEventFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        eventDetails = Util.BundleToHashMap(getArguments());
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        eventTitle_editText.setText(eventDetails.get(Event.COLUMN_TITLE));
        location_autoComplete.setText(eventDetails.get(Event.COLUMN_LOCATION_NAME));
        eventDescription_editText.setText(eventDetails.get(Event.COLUMN_DESCRIPTION));

        String startDate =
                eventDetails.get(Event.COLUMN_START_YEAR) + ":" +
                        eventDetails.get(Event.COLUMN_START_MONTH) + ":" +
                        eventDetails.get(Event.COLUMN_START_DAY);
        String startTime = eventDetails.get(Event.COLUMN_START_HOUR) + ":" +
                eventDetails.get(Event.COLUMN_START_MINUTE);
        chooseStartDate.setText(startDate);
        chooseStartTime.setText(startTime);

        String endDate =
                eventDetails.get(Event.COLUMN_END_YEAR) + ":" +
                        eventDetails.get(Event.COLUMN_END_MONTH) + ":" +
                        eventDetails.get(Event.COLUMN_END_DAY);
        String endTime =
                eventDetails.get(Event.COLUMN_END_HOUR) + ":" +
                        eventDetails.get(Event.COLUMN_END_MINUTE);
        chooseEndDate.setText(endDate);
        chooseEndTime.setText(endTime);

        eventTag_editText.setText(eventDetails.get(Event.COLUMN_TAG));
        shoutEvent_checkBox.setChecked(Boolean.valueOf(eventDetails.get(Event.COLUMN_SHOUT)).equals(true));
        return rootView;
    }

    @Override
    public void setState() {
        final String userId = getActivity().getIntent().getStringExtra("user_id");
        if (eventDetails.get(Event.COLUMN_CREATOR_ID).equals(userId)) {
            STATE = getResources().getInteger(R.integer.confirm_changes);
            createEvent_Button.setText(getText(R.string.confirm_changes).toString());
        } else if (eventDetails.get(Invite.COLUMN_INVITEE_ID) != null &&
                !eventDetails.get(Invite.COLUMN_INVITEE_ID).equals("null")) {
            if (eventDetails.get(Invite.COLUMN_INVITEE_ID).equals(userId)) {
                if (eventDetails.get(Invite.COLUMN_GOING).equals("Yes")) {
                    STATE = getResources().getInteger(R.integer.unfollow_event);
                    createEvent_Button.setText(getText(R.string.unfollow_event).toString());
                    friends_tableRow.setVisibility(View.GONE);
                } else {
                    STATE = getResources().getInteger(R.integer.accept_invite);
                    createEvent_Button.setText(getText(R.string.accept_invite).toString());
                    friends_tableRow.setVisibility(View.GONE);
                }
            }
        } else {
            STATE = getResources().getInteger(R.integer.join_event);
            createEvent_Button.setText(getText(R.string.join_event).toString());
            friends_area.setVisibility(View.GONE);
        }

        createEvent_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (STATE != getResources().getInteger(R.integer.confirm_changes)) {
                    if (STATE == getResources().getInteger(R.integer.join_event)) {
                        eventDetails.put(Invite.COLUMN_INVITEE_ID, userId);
                        eventDetails.put(Invite.COLUMN_GOING, "Yes");
                        eventDetails.put(Invite.COLUMN_SENT, "Yes");
                        updateEvent(true);
                    } else if (STATE == getResources().getInteger(R.integer.accept_invite)) {
                        eventDetails.put(Invite.COLUMN_GOING, "Yes");
                        updateEvent(true);
                    } else if (STATE == getResources().getInteger(R.integer.unfollow_event)) {
                        eventDetails.put(Invite.COLUMN_GOING, "No");
                        updateEvent(true);
                    }
                } else if (STATE == getResources().getInteger(R.integer.confirm_changes)) {
                    updateEvent(false);
                }
            }
        });
    }
}