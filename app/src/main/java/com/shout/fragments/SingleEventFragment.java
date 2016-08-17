package com.shout.fragments;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.shout.R;
import com.shout.applications.ShoutApplication;
import com.shout.customViews.FriendsView;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public abstract class SingleEventFragment extends Fragment {
    public final String CREATE_EVENT_PHP_PATH = "http://shouttestserver.ueuo.com/create_event.php";
    EditText eventTitle_editText, eventLocation_editText, eventDescription_editText,
            eventTag_editText;
    TextView chooseStartDate, chooseStartTime, chooseEndDate, chooseEndTime;
    FriendsView eventInvitees_editText;
    CheckBox shoutEvent_checkBox;
    FlexboxLayout friends_area;
    Button createEvent_Button;
    HashMap<String, String> eventInvite = new HashMap<>();

    public abstract void configureButton() throws JSONException;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.create_event_fragment, container, false);
        eventTitle_editText = (EditText) rootView.findViewById(R.id.eventTitle_editText);
        eventLocation_editText = (EditText) rootView.findViewById(R.id.eventLocation_editText);
        eventDescription_editText = (EditText) rootView.findViewById(R.id
                .eventDescription_editText);
        chooseStartDate = (TextView) rootView.findViewById(R.id.chooseStartDate_textView);
        chooseStartTime = (TextView) rootView.findViewById(R.id.chooseStartTime_textView);
        chooseEndDate = (TextView) rootView.findViewById(R.id.chooseEndDate_textView);
        chooseEndTime = (TextView) rootView.findViewById(R.id.chooseEndTime_textView);
        eventTag_editText = (EditText) rootView.findViewById(R.id.eventTag_editText);
        friends_area = (FlexboxLayout) rootView.findViewById(R.id.friendsArea_flexBox);
        eventInvitees_editText = (FriendsView) rootView.findViewById(R.id.eventInvitees_textView);
        eventInvitees_editText.setRoot((ViewGroup) eventInvitees_editText.getParent());
        shoutEvent_checkBox = (CheckBox) rootView.findViewById(R.id.shoutEvent_checkBox);
        createEvent_Button = (Button) rootView.findViewById(R.id.createEvent_button);
        try {
            Intent intent = getActivity().getIntent();
            ArrayList<String> friendIds = intent.getStringArrayListExtra("friendIds");
            ArrayList<String> friendNames = intent.getStringArrayListExtra("friendNames");
            ArrayList<JSONObject> pairs = new ArrayList<>();
            for (int i = 0; i < friendIds.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", friendIds.get(i));
                jsonObject.put("name", friendNames.get(i));
                pairs.add(jsonObject);
            }
            eventInvitees_editText.setAdapter(new FriendsAdapter(getContext(), pairs));
            eventInvitees_editText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

            Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            final int minute = calendar.get(Calendar.MINUTE);

            chooseStartDate.setText(String.format(Locale.ENGLISH, "%1$d:%2$d:%3$d", year, month,
                    day));
            chooseStartTime.setText(String.format(Locale.ENGLISH, "%1$d:%2$d", hour, minute));
            chooseEndDate.setText(String.format(Locale.ENGLISH, "%1$d:%2$d:%3$d", year, month,
                    day));
            chooseEndTime.setText(String.format(Locale.ENGLISH, "%1$d:%2$d", hour, minute));

            View.OnClickListener dateListener = new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    OnDateSetListener listener = new OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                            ((TextView) view).setText(String.format(Locale.ENGLISH,
                                    "%1$d:%2$d:%3$d", y, m, d));
                        }
                    };
                    DatePickerDialog dialog = new DatePickerDialog(getContext(), listener, year,
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
                            ((TextView) view).setText(String.format(Locale.ENGLISH, "%1$d:%2$d",
                                    h, m));
                        }
                    };
                    TimePickerDialog dialog = new TimePickerDialog(getContext(), listener, hour,
                            minute, true);
                    dialog.setTitle("Set the Start Time of your Event");
                    dialog.show();
                }
            };

            chooseStartDate.setOnClickListener(dateListener);
            chooseEndDate.setOnClickListener(dateListener);
            chooseStartTime.setOnClickListener(timeListener);
            chooseEndTime.setOnClickListener(timeListener);
            configureButton();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootView;
    }

    public void captureUserData() {
        eventInvite.put(Event.COLUMN_TITLE, eventTitle_editText.getText().toString());
        eventInvite.put(Event.COLUMN_LOCATION, eventLocation_editText.getText().toString());
        eventInvite.put(Event.COLUMN_DESCRIPTION, (eventDescription_editText.getText().toString()));
        eventInvite.put(Event.COLUMN_START_DATETIME, chooseStartDate.getText().toString() + ":"
                + chooseStartTime.getText().toString());
        eventInvite.put(Event.COLUMN_END_DATETIME, chooseEndDate.getText().toString() + ":" +
                chooseEndTime.getText().toString());
        eventInvite.put(Event.COLUMN_TAG, eventTag_editText.getText().toString());
        eventInvite.put(Event.COLUMN_SHOUT, String.valueOf(shoutEvent_checkBox.isChecked()));
    }

    public void updateEvent(Boolean newEvent, Boolean updateInvite) {
        try {
            captureUserData();
            JSONObject jsonObject = new JSONObject(eventInvite);
            JSONArray jsonArray = new JSONArray();
            for (String invitee : eventInvitees_editText.invitees) {
                jsonArray = jsonArray.put(invitee);
            }
            jsonObject.put("invitees", jsonArray);
            if (newEvent) {
                jsonObject.put("new_event", "Yes");
                jsonObject.put(Event.COLUMN_EVENT_ID, JSONObject.NULL);
            } else {
                jsonObject.put("new_event", "No");
            }
            String path = updateInvite ? EventsListFragment.UPDATE_GOING_PHP_PATH :
                    CREATE_EVENT_PHP_PATH;
            new UpdateEventTask().execute(new Pair<>(path, new Pair<>(updateInvite, jsonObject)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class UpdateEventTask extends ShoutApplication.SendAndReceiveJSON<Boolean> {
        @Override
        public void onPostExecute(Pair<Boolean, JSONObject> pair) {
            try {
                if (pair.second.get("insert").equals("Success!")) {
                    HashMap<String, String> eventInvite = ShoutApplication.JSONObjectToHashMap(pair
                            .second.getJSONObject("event_invite"));
                    Pair<Integer, Integer> result = ShoutApplication.updateEventInvite(eventInvite,
                            getContext(), pair.first);
                    if (result.first.equals(1) && (!pair.first || result.second.equals(1))) {
                        getFragmentManager().popBackStack();
                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_LONG).show();
                    } else {
                        String localError = "Error updating local database";
                        Toast.makeText(getContext(), localError, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String remoteError = pair.second.getString("error_message");
                    Toast.makeText(getContext(), remoteError, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class FriendsAdapter extends BaseAdapter implements Filterable {
        private Context context;
        private ArrayList<JSONObject> originalData;
        private ArrayList<JSONObject> filteredData;
        private FriendsFilter friendsFilter;

        public FriendsAdapter(Context context, ArrayList<JSONObject> originalData) {
            this.context = context;
            this.originalData = originalData;
            this.friendsFilter = new FriendsFilter();
        }

        @Override
        public int getCount() {
            return (filteredData == null) ? 0 : filteredData.size();
        }

        @Override
        public Object getItem(int i) {
            return (filteredData == null) ? null : filteredData.get(i).toString();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.canoncial_text_view,
                        parent, false);
                holder = new ViewHolder();
                holder.view = convertView.findViewById(R.id.content_textView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            JSONObject jsonObject = filteredData.get(position);
            TextView textView = (TextView) holder.view.findViewById(R.id.content_textView);
            textView.setText(jsonObject.optString("name"));
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return friendsFilter;
        }

        class ViewHolder {
            View view;
        }

        private class FriendsFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterString = charSequence.toString().toLowerCase();
                ArrayList<JSONObject> data = new ArrayList<>();
                for (JSONObject jsonObject : originalData) {
                    if (jsonObject.optString("name").toLowerCase().contains(filterString)) {
                        data.add(jsonObject);
                    }
                }
                FilterResults results = new FilterResults();
                results.values = data;
                results.count = data.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList<JSONObject>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }
}