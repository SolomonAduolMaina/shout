package com.shout.fragments;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.shout.R;
import com.shout.activities.ShoutActivity;
import com.shout.customViews.FriendsView;
import com.shout.database.DatabaseUtilities;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.networkmessaging.SendMessages;
import com.shout.utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public abstract class SingleEventFragment extends Fragment {
    PlaceAutocompleteFragment location_autoComplete;
    EditText eventTitle_editText, eventDescription_editText, eventTag_editText;
    TextView chooseStartDate, chooseStartTime, chooseEndDate, chooseEndTime;
    FriendsView eventInvitees_editText;
    CheckBox shoutEvent_checkBox;
    FlexboxLayout friends_area;
    TableRow friends_tableRow;
    Button createEvent_Button;

    int STATE;
    HashMap<String, String> eventDetails;

    public abstract void setState();

    @Override
    public void onPause() {
        Intent intent = new Intent(ShoutActivity.ACTION_FRAGMENT_PAUSED);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        super.onPause();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.create_event_fragment, container, false);

        if (eventDetails == null) {
            eventDetails = new HashMap<>();
            for (String column : Event.COLUMNS) {
                eventDetails.put(column, "");
            }
        }

        createEvent_Button = (Button) rootView.findViewById(R.id.createEvent_button);

        shoutEvent_checkBox = (CheckBox) rootView.findViewById(R.id.shoutEvent_checkBox);
        shoutEvent_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                eventDetails.put(Event.COLUMN_SHOUT, String.valueOf(b));
            }
        });

        eventTitle_editText = (EditText) rootView.findViewById(R.id.eventTitle_editText);
        eventTag_editText = (EditText) rootView.findViewById(R.id.eventTag_editText);
        eventDescription_editText = (EditText) rootView.findViewById(R.id.eventDescription_editText);

        View.OnFocusChangeListener textChanged = new View.OnFocusChangeListener() {
            @Override

            public void onFocusChange(View view, boolean b) {
                if (view.equals(eventTitle_editText)) {
                    eventDetails.put(Event.COLUMN_TITLE, eventTitle_editText.getText().toString());
                } else if (view.equals(eventDescription_editText)) {
                    eventDetails.put(Event.COLUMN_DESCRIPTION,
                            eventDescription_editText.getText().toString());
                } else if (view.equals(eventTag_editText)) {
                    eventDetails.put(Event.COLUMN_TAG, eventTag_editText.getText().toString());
                }
            }
        };

        eventTitle_editText.setOnFocusChangeListener(textChanged);
        eventTag_editText.setOnFocusChangeListener(textChanged);
        eventDescription_editText.setOnFocusChangeListener(textChanged);

        chooseStartDate = (TextView) rootView.findViewById(R.id.chooseStartDate_textView);
        chooseStartTime = (TextView) rootView.findViewById(R.id.chooseStartTime_textView);
        chooseEndDate = (TextView) rootView.findViewById(R.id.chooseEndDate_textView);
        chooseEndTime = (TextView) rootView.findViewById(R.id.chooseEndTime_textView);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        View.OnClickListener dateListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                OnDateSetListener listener = new OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        if (view.equals(chooseStartDate)) {
                            eventDetails.put(Event.COLUMN_START_YEAR, String.valueOf(y));
                            eventDetails.put(Event.COLUMN_START_MONTH, String.valueOf(m));
                            eventDetails.put(Event.COLUMN_START_DAY, String.valueOf(d));
                            chooseStartDate.setText(String.format(Locale.ENGLISH, "%d:%d:%d", y, m, d));
                        } else {
                            eventDetails.put(Event.COLUMN_END_YEAR, String.valueOf(y));
                            eventDetails.put(Event.COLUMN_END_MONTH, String.valueOf(m));
                            eventDetails.put(Event.COLUMN_END_DAY, String.valueOf(d));
                            chooseEndDate.setText(String.format(Locale.ENGLISH, "%d:%d:%d", y, m, d));
                        }
                    }
                };
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), listener, year,
                        month, day);
                dialog.setTitle("Set the Start Date of your Event");
                dialog.show();
            }
        };

        View.OnClickListener timeListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                OnTimeSetListener listener = new OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        if (view.equals(chooseStartTime)) {
                            eventDetails.put(Event.COLUMN_START_HOUR, String.valueOf(h));
                            eventDetails.put(Event.COLUMN_START_MINUTE, String.valueOf(m));
                            chooseStartTime.setText(String.format(Locale.ENGLISH, "%d:%d", h, m));
                        } else {
                            eventDetails.put(Event.COLUMN_END_HOUR, String.valueOf(h));
                            eventDetails.put(Event.COLUMN_END_MINUTE, String.valueOf(m));
                            chooseEndTime.setText(String.format(Locale.US, "%d:%d", h, m));
                        }
                    }
                };
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), listener, hour,
                        minute, true);
                dialog.setTitle("Set the Start Time of your Event");
                dialog.show();
            }
        };

        chooseStartDate.setText(String.format(Locale.ENGLISH, "%1$d:%2$d:%3$d", year, month, day));
        chooseStartTime.setText(String.format(Locale.ENGLISH, "%1$d:%2$d", hour, minute));
        chooseEndDate.setText(String.format(Locale.ENGLISH, "%1$d:%2$d:%3$d", year, month, day));
        chooseEndTime.setText(String.format(Locale.ENGLISH, "%1$d:%2$d", hour, minute));

        chooseStartDate.setOnClickListener(dateListener);
        chooseEndDate.setOnClickListener(dateListener);
        chooseStartTime.setOnClickListener(timeListener);
        chooseEndTime.setOnClickListener(timeListener);

        Intent intent = getActivity().getIntent();
        ArrayList<String> friendIds = intent.getStringArrayListExtra("friend_ids");
        ArrayList<String> friendNames = intent.getStringArrayListExtra("friend_names");
        ArrayList<Pair<String, String>> pairs = new ArrayList<>();
        for (int i = 0; i < friendIds.size(); i++) {
            pairs.add(new Pair<>(friendIds.get(i), friendNames.get(i)));
        }

        friends_tableRow = (TableRow) rootView.findViewById(R.id.friends_tableRow);
        friends_area = (FlexboxLayout) rootView.findViewById(R.id.friendsArea_flexBox);
        eventInvitees_editText = (FriendsView) rootView.findViewById(R.id.eventInvitees_textView);
        eventInvitees_editText.setRoot((ViewGroup) eventInvitees_editText.getParent());
        eventInvitees_editText.setFriendsAdapter(this.getActivity(), pairs);
        eventInvitees_editText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        location_autoComplete = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.location_autoComplete);
        location_autoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng location = place.getLatLng();
                eventDetails.put(Event.COLUMN_LATITUDE, String.valueOf(location.latitude));
                eventDetails.put(Event.COLUMN_LONGITUDE, String.valueOf(location.longitude));
                eventDetails.put(Event.COLUMN_LOCATION_NAME, place.getAddress().toString());
            }

            @Override
            public void onError(Status status) {
                // TODO Handle on place selected error
            }
        });


        setState();
        return rootView;
    }


    public void updateEvent(Boolean updateInvite) {
        JSONObject jsonObject = new JSONObject(eventDetails);
        JSONArray jsonArray = new JSONArray();
        if (STATE == getResources().getInteger(R.integer.create_event)
                || STATE == getResources().getInteger(R.integer.confirm_changes)) {
            for (String invitee : eventInvitees_editText.invitees) {
                jsonArray = jsonArray.put(invitee);
            }
        }
        try {
            jsonObject.put("invitees", jsonArray);
            if (STATE == getResources().getInteger(R.integer.create_event)) {
                jsonObject.put("new_event", "Yes");
                jsonObject.put(Event.COLUMN_CREATOR_ID,
                        getActivity().getIntent().getStringExtra("user_id"));
                jsonObject.put(Event.COLUMN_EVENT_ID, JSONObject.NULL);
            } else {
                jsonObject.put("new_event", "No");
            }
            updateEventTask(new Pair<>(updateInvite, jsonObject));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateEventTask(final Pair<Boolean, JSONObject> pair) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) throws JSONException {
                if (response.get("result").equals("Success!")) {
                    HashMap<String, String> eventInvite = Util.JSONObjectToHashMap(response
                            .getJSONObject("event_invite"));
                    Pair<Integer, Integer> result = DatabaseUtilities.updateLocalDatabase(eventInvite,
                            getActivity(), pair.first);
                    if (result.first.equals(1) && (!pair.first || result.second.equals(1))) {
                        Intent intent = new Intent(MyEventsFragment.ACTION_UPDATED_DATABASE);
                        intent.putExtra("notification", "No");
                        intent.putExtra("event_id", "None");
                        intent.putExtra("type", "None");
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        getFragmentManager().popBackStack();
                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
                    } else {
                        String localError = "Error updating local database";
                        Toast.makeText(getActivity(), localError, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String remoteError = pair.second.getString("error_message");
                    Toast.makeText(getActivity(), remoteError, Toast.LENGTH_LONG).show();
                }

            }
        };
        String path = pair.first ? getString(R.string.update_going_php_path) :
                getString(R.string.create_event_php_path);
        SendMessages.doOnResponse(lambda, getActivity(), pair.second, path);
    }
}