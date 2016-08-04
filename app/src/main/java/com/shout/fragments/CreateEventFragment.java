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
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewGroup;

import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.MultiAutoCompleteTextView;

import com.google.android.flexbox.FlexboxLayout;
import com.shout.R;
import com.shout.applications.ShoutApplication;
import com.shout.customViews.FriendsView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class CreateEventFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String CREATE_EVENT_PHP_PATH = "http://shouttestserver.ueuo.com/create_event.php";

    EditText eventTitle_editText, eventLocation_editText, eventDescription_editText,
            eventTag_editText;
    TextView chooseStartDate, chooseStartTime, chooseEndDate, chooseEndTime;
    FriendsView eventInvitees_editText;
    CheckBox shoutEvent_checkBox;
    FlexboxLayout friends_area;

    public CreateEventFragment() {
    }

    public static CreateEventFragment newInstance(int sectionNumber) {
        CreateEventFragment fragment = new CreateEventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

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
        eventInvitees_editText.setRoot((ViewGroup)eventInvitees_editText.getParent());
        shoutEvent_checkBox = (CheckBox) rootView.findViewById(R.id.shoutEvent_checkBox);
        Button createEvent_Button = (Button) rootView.findViewById(R.id.createEvent_button);

        Intent intent = getActivity().getIntent();
        ArrayList<String> friendIds = intent.getStringArrayListExtra("friendIds");
        ArrayList<String> friendNames = intent.getStringArrayListExtra("friendNames");
        ArrayList<Pair<String, String>> pairs = new ArrayList<>();
        for (int i = 0; i < friendIds.size(); i++) {
            pairs.add(new Pair<>(friendIds.get(i), friendNames.get(i)));
        }
        eventInvitees_editText.setAdapter(new FriendsAdapter(getContext(), pairs));
        eventInvitees_editText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        chooseStartDate.setText(String.format(Locale.ENGLISH, "%1$d:%2$d:%3$d", year, month, day));
        chooseStartTime.setText(String.format(Locale.ENGLISH, "%1$d:%2$d", hour, minute));
        chooseEndDate.setText(String.format(Locale.ENGLISH, "%1$d:%2$d:%3$d", year, month, day));
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
                        ((TextView) view).setText(String.format(Locale.ENGLISH, "%1$d:%2$d", h, m));
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

        createEvent_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try {
                    Bundle bundle = getActivity().getIntent().getExtras();
                    jsonObject.put("creator_id", "'" + bundle.getString("userId") + "'");
                    jsonObject.put("title", "'" + eventTitle_editText.getText().toString() + "'");
                    jsonObject.put("location", "'" + eventLocation_editText.getText().toString()
                            + "'");
                    jsonObject.put("description", "'" + eventDescription_editText.getText()
                            .toString()
                            + "'");
                    jsonObject.put("start_datetime", "'" + chooseStartDate.getText().toString() +
                            ":" + chooseStartTime.getText().toString() + "'");
                    jsonObject.put("end_datetime", "'" + chooseEndDate.getText().toString() + ":" +
                            chooseEndTime.getText().toString() + "'");
                    jsonObject.put("tag", "'" + eventTag_editText.getText().toString() + "'");
                    jsonObject.put("shout", "'" + String.valueOf(shoutEvent_checkBox.isChecked())
                            + "'");
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < friends_area.getChildCount() - 1; i++) {
                        TextView textView = (TextView) friends_area.getChildAt(i);
                        jsonArray = jsonArray.put(i, "'" + textView.getText().toString() + "'");
                    }
                    jsonObject.put("invitees", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new CreateEventTask().execute(new Pair<>(CREATE_EVENT_PHP_PATH, new Pair<>(
                        (Void) null, jsonObject)));
            }
        });
        return rootView;
    }

    private class CreateEventTask extends ShoutApplication.SendAndReceiveJSON<Void> {

        @Override
        public void onPostExecute(Pair<Void, JSONObject> pair) {
            try {
                String insert = pair.second.getString("insert");
                String errorMessage = pair.second.getString("error_message");
                if (insert.equals("Success!")) {
                    eventTitle_editText.setText("");
                    eventLocation_editText.setText("");
                    eventDescription_editText.setText("");
                    chooseStartDate.setText("");
                    chooseStartTime.setText("");
                    chooseEndDate.setText("");
                    chooseEndTime.setText("");
                    eventTag_editText.setText("");
                    shoutEvent_checkBox.setChecked(false);
                    for (int i = 0; i < friends_area.getChildCount() - 1; i++) {
                        friends_area.removeViewAt(i);
                    }
                    Toast.makeText(getContext(), insert, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class FriendsAdapter extends BaseAdapter implements Filterable {

        private Context context;
        private ArrayList<Pair<String, String>> originalData;
        private ArrayList<Pair<String, String>> filteredData;
        private FriendsFilter friendsFilter;

        public FriendsAdapter(Context context, ArrayList<Pair<String, String>> originalData) {
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
            Pair<String, String> pair = filteredData.get(i);
            return (filteredData == null) ? null : pair.second;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.test_text_view,
                         parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.content_textView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Pair<String, String> pair = filteredData.get(position);
            holder.textView.setText(pair.second);
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return friendsFilter;
        }

        class ViewHolder {
            TextView textView;
        }

        private class FriendsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterString = charSequence.toString().toLowerCase();
                ArrayList<Pair<String, String>> data = new ArrayList<>();
                for (Pair<String, String> pair : originalData) {
                    if (pair.second.toLowerCase().contains(filterString)) {
                        data.add(pair);
                    }
                }
                FilterResults results = new FilterResults();
                results.values = data;
                results.count = data.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList<Pair<String, String>>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }
}