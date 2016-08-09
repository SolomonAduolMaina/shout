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
import com.shout.applications.ShoutApplication.SendAndReceiveJSON;
import com.shout.notificationsProvider.NotificationsProvider;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;
import com.shout.wrapperClasses.WrapperClasses.EventInvite;
import com.shout.wrapperClasses.WrapperClasses.EventInviteClasses;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class EventsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        userId = getActivity().getIntent().getStringExtra("userId");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.events_fragment, container, false);

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
        private EventInviteClasses eventInviteClasses;
        private int tab = 0;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View view;
            public EventInvite eventInvite;

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
            if (eventInviteClasses != null) {
                ArrayList<EventInvite> data = null;
                switch (tab) {
                    case ALL:
                        data = eventInviteClasses.all;
                        break;
                    case MY_EVENTS:
                        data = eventInviteClasses.myEvents;
                        break;
                    case INVITED:
                        data = eventInviteClasses.invited;
                        break;
                    case SUGGESTED:
                        data = eventInviteClasses.suggested;
                        break;
                }
                holder.eventInvite = data.get(position);
                ((TextView) holder.view.findViewById(R.id.title_textView)).setText(holder
                        .eventInvite.title);
                ((TextView) holder.view.findViewById(R.id.location_textView)).setText(holder
                        .eventInvite.location);
                ((TextView) holder.view.findViewById(R.id.startDateTime_textView)).setText(holder
                        .eventInvite.startDateTime);
                Button yesButton = (Button) holder.view.findViewById(R.id.button);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.eventInvite.going = "Yes";
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("going", "'" + holder.eventInvite.going + "'");
                            jsonObject.put("invitee_id", "'" + holder.eventInvite.inviteeId + "'");
                            jsonObject.put("event_id", "'" + holder.eventInvite.eventId + "'");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new UpdateEventTask().execute(new Pair<>(UPDATE_GOING_PHP_PATH, new
                                Pair<>(holder, jsonObject)));
                    }
                });
                Button noButton = (Button) holder.view.findViewById(R.id.button2);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.eventInvite.going = "No";
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("going", "'" + holder.eventInvite.going + "'");
                            jsonObject.put("invitee_id", "'" + holder.eventInvite.inviteeId + "'");
                            jsonObject.put("event_id", "'" + holder.eventInvite.eventId + "'");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new UpdateEventTask().execute(new Pair<>(UPDATE_GOING_PHP_PATH, new
                                Pair<>(holder, jsonObject)));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (eventInviteClasses != null) {
                switch (tab) {
                    case ALL:
                        return eventInviteClasses.all.size();
                    case MY_EVENTS:
                        return eventInviteClasses.myEvents.size();
                    case INVITED:
                        return eventInviteClasses.invited.size();
                    case SUGGESTED:
                        return eventInviteClasses.suggested.size();
                }
            }
            return 0;
        }

        public void setData(Cursor cursor, String userId) {
            eventInviteClasses = new EventInviteClasses(cursor, userId);
            notifyDataSetChanged();
        }

        public void changeTab(int tab) {
            this.tab = tab;
            notifyDataSetChanged();
        }
    }

    private class UpdateEventTask extends SendAndReceiveJSON<EventsAdapter.ViewHolder> {

        @Override
        public void onPostExecute(Pair<EventsAdapter.ViewHolder, JSONObject> pair) {
            try {
                if (pair.second.getString("update").equals("Success!")) {
                    EventInvite eventInvite = pair.first.eventInvite;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Invite.COLUMN_INVITEE_ID, eventInvite.inviteeId);
                    contentValues.put(Invite.COLUMN_EVENT_ID, eventInvite.eventId);
                    contentValues.put(Invite.COLUMN_TYPE, eventInvite.type);
                    contentValues.put(Invite.COLUMN_GOING, eventInvite.going);
                    contentValues.put(Invite.COLUMN_SENT, eventInvite.sent);
                    int rows = getContext().getContentResolver().update(NotificationsProvider
                            .INVITE_URI, contentValues, Invite.COLUMN_INVITEE_ID + "= ? AND " +
                            Invite.COLUMN_EVENT_ID + " = ?", new String[]{eventInvite.inviteeId,
                            eventInvite.eventId});
                    if (rows > 0) {
                        EventsAdapter adapter = (EventsAdapter) eventsRecyclerView.getAdapter();
                        int position = eventsRecyclerView.getChildAdapterPosition(pair.first.view);
                        switch (adapter.tab) {
                            case ALL:
                                adapter.eventInviteClasses.all.remove(position);
                            case MY_EVENTS:
                                adapter.eventInviteClasses.myEvents.remove(position);
                            case INVITED:
                                adapter.eventInviteClasses.invited.remove(position);
                            case SUGGESTED:
                                adapter.eventInviteClasses.suggested.remove(position);
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