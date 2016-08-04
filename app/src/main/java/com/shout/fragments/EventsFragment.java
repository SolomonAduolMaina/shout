package com.shout.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;

import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shout.R;
import com.shout.activities.ShoutActivity;
import com.shout.applications.ShoutApplication;
import com.shout.notificationsProvider.NotificationsProvider;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;
import com.shout.wrapperClasses.WrapperClasses.EventDetails;
import com.shout.wrapperClasses.WrapperClasses.EventDetailClasses;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class EventsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String UPDATE_GOING_PHP_PATH = "http://shouttestserver.ueuo.com/update_going.php";
    private final int ALL = 0;
    private final int MY_EVENTS = 1;
    private final int INVITED = 2;
    private final int SUGGESTED = 3;

    private String userId;
    RecyclerView eventsRecyclerView;
    TabLayout eventsTabLayout;

    private BroadcastReceiver eventsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor data = getActivity().getContentResolver().query(NotificationsProvider
                            .NOTIFICATIONS_URI, null, "Event.creator_id = ? OR Invite.invitee_id " +
                            " = ?",
                    new String[]{userId, userId}, null);
            ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(data, userId);
        }
    };

    public EventsFragment() {
    }

    public static EventsFragment newInstance(int sectionNumber) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.events_fragment, container, false);

        userId = getActivity().getIntent().getStringExtra("userId");

        eventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.events_recyclerView);
        eventsTabLayout = (TabLayout) rootView.findViewById(R.id.events_tabLayout);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventsRecyclerView.setAdapter(new EventsAdapter());

        eventsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                ((EventsAdapter) eventsRecyclerView.getAdapter()).changeTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab) {
            }

            @Override
            public void onTabReselected(Tab tab) {
            }
        });

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), NotificationsProvider.NOTIFICATIONS_URI, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(data, userId);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(null, userId);

    }


    @Override
    public void onResume() {
        getContext().registerReceiver(eventsReceiver, ShoutActivity.syncIntentFilter);
        super.onResume();
    }

    @Override
    public void onPause() {
        getContext().unregisterReceiver(eventsReceiver);
        super.onPause();
    }


    public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

        private EventDetailClasses eventDetailClasses;
        private int tab = 0;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View view;
            public EventDetails notification;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
            }
        }

        public EventsAdapter() {
            super();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .event, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (eventDetailClasses != null) {
                ArrayList<EventDetails> data = null;
                switch (tab) {
                    case ALL:
                        data = eventDetailClasses.all;
                        break;
                    case MY_EVENTS:
                        data = eventDetailClasses.myEvents;
                        break;
                    case INVITED:
                        data = eventDetailClasses.invited;
                        break;
                    case SUGGESTED:
                        data = eventDetailClasses.suggested;
                        break;
                }
                holder.notification = data.get(position);
                ((TextView) holder.view.findViewById(R.id.title_textView)).setText(holder
                        .notification.title);
                ((TextView) holder.view.findViewById(R.id.location_textView)).setText(holder
                        .notification.location);
                ((TextView) holder.view.findViewById(R.id.startDateTime_textView)).setText(holder
                        .notification.startDateTime);
                Button yesButton = (Button) holder.view.findViewById(R.id.button);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.notification.going = "Yes";
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("going", "'" + holder.notification.going + "'");
                            jsonObject.put("invitee_id", "'" + holder.notification.inviteeId + "'");
                            jsonObject.put("event_id", "'" + holder.notification.eventId + "'");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new UpdateNotificationTask().execute(new Pair<>(UPDATE_GOING_PHP_PATH, new
                                Pair<>(holder, jsonObject)));
                    }
                });
                Button noButton = (Button) holder.view.findViewById(R.id.button2);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.notification.going = "No";
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("going", "'" + holder.notification.going + "'");
                            jsonObject.put("invitee_id", "'" + holder.notification.inviteeId + "'");
                            jsonObject.put("event_id", "'" + holder.notification.eventId + "'");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new UpdateNotificationTask().execute(new Pair<>(UPDATE_GOING_PHP_PATH, new
                                Pair<>(holder, jsonObject)));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (eventDetailClasses != null) {
                switch (tab) {
                    case ALL:
                        return eventDetailClasses.all.size();
                    case MY_EVENTS:
                        return eventDetailClasses.myEvents.size();
                    case INVITED:
                        return eventDetailClasses.invited.size();
                    case SUGGESTED:
                        return eventDetailClasses.suggested.size();
                }
            }
            return 0;
        }

        public void setData(Cursor cursor, String userId) {
            eventDetailClasses = new EventDetailClasses(cursor, userId);
            notifyDataSetChanged();
        }

        public void changeTab(int tab) {
            this.tab = tab;
            notifyDataSetChanged();
        }
    }

    private class UpdateNotificationTask extends ShoutApplication
            .SendAndReceiveJSON<EventsAdapter.ViewHolder> {

        @Override
        public void onPostExecute(Pair<EventsAdapter.ViewHolder, JSONObject> pair) {
            try {
                if (pair.second.getString("update").equals("Success!")) {
                    EventDetails notification = pair.first.notification;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Invite.COLUMN_INVITEE_ID, notification.inviteeId);
                    contentValues.put(Invite.COLUMN_EVENT_ID, notification.eventId);
                    contentValues.put(Invite.COLUMN_TYPE, notification.type);
                    contentValues.put(Invite.COLUMN_GOING, notification.going);
                    contentValues.put(Invite.COLUMN_SENT, notification.sent);
                    int rows = getContext().getContentResolver().update(NotificationsProvider
                            .INVITE_URI, contentValues, Invite.COLUMN_INVITEE_ID + "= ? AND " +
                            Invite.COLUMN_EVENT_ID + " = ?", new String[]{notification.inviteeId,
                            notification.eventId});
                    if (rows > 0) {
                        EventsAdapter adapter = (EventsAdapter) eventsRecyclerView
                                .getAdapter();
                        int position = eventsRecyclerView.getChildAdapterPosition(pair.first.view);
                        switch (adapter.tab) {
                            case ALL:
                                adapter.eventDetailClasses.all.remove(position);
                            case MY_EVENTS:
                                adapter.eventDetailClasses.myEvents.remove(position);
                            case INVITED:
                                adapter.eventDetailClasses.invited.remove(position);
                            case SUGGESTED:
                                adapter.eventDetailClasses.suggested.remove(position);
                        }
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_LONG).show();
                    } else {
                        String localError = "Error updating local database";
                        Toast.makeText(getContext(), localError, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String serverError = pair.second.getString("error_message");
                    Toast.makeText(getContext(), serverError, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_LONG).show();
            }
        }
    }
}