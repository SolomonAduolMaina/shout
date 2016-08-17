package com.shout.fragments;

import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;

import android.database.Cursor;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.shout.applications.ShoutApplication;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Invite;
import com.shout.notificationsProvider.ShoutDatabaseDescription.Event;

import com.shout.applications.ShoutApplication.EventInviteClasses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class EventsListFragment extends Fragment {
    final static String UPDATE_GOING_PHP_PATH = "http://shouttestserver.ueuo.com/update_going.php";
    final int ALL = 0;
    final int MY_EVENTS = 1;
    final int INVITED = 2;
    final int SUGGESTED = 3;

    RecyclerView eventsRecyclerView;
    TabLayout eventsTabLayout;
    String userId;

    public abstract void setYesOnClickListener(Button button, EventsAdapter.ViewHolder holder);

    public abstract void setNoOnClickListener(Button button, EventsAdapter.ViewHolder holder);

    public View.OnClickListener setUpdateInviteListener(final EventsListFragment.EventsAdapter
            .ViewHolder holder, final String goingValue) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    holder.eventInvite.put(Invite.COLUMN_GOING, goingValue);
                    JSONObject jsonObject = new JSONObject(holder.eventInvite);
                    new UpdateEventsView().execute(new Pair<>(UPDATE_GOING_PHP_PATH, new
                            Pair<>(holder, jsonObject)));

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        userId = getActivity().getIntent().getStringExtra("userId");
        View rootView = inflater.inflate(R.layout.events_fragment, container, false);

        eventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.events_recyclerView);
        eventsTabLayout = (TabLayout) rootView.findViewById(R.id.events_tabLayout);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

    public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
        public EventInviteClasses eventInviteClasses;
        public int tab = 0;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View view;
            public HashMap<String, String> eventInvite;

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
                ArrayList<HashMap<String, String>> data = null;
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
                        .eventInvite.get(Event.COLUMN_TITLE));
                ((TextView) holder.view.findViewById(R.id.location_textView)).setText(holder
                        .eventInvite.get(Event.COLUMN_LOCATION));
                ((TextView) holder.view.findViewById(R.id.startDateTime_textView)).setText
                        (holder.eventInvite.get(Event.COLUMN_START_DATETIME));
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ViewEventFragment eventFragment = new ViewEventFragment();
                        eventFragment.setArguments(ShoutApplication.HashMapToBundle(holder
                                .eventInvite));
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment, eventFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });
                setYesOnClickListener((Button) holder.view.findViewById(R.id.button), holder);
                setNoOnClickListener((Button) holder.view.findViewById(R.id.button2), holder);
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

        public void setData(JSONArray data) throws JSONException {
            eventInviteClasses = new EventInviteClasses(data);
            notifyDataSetChanged();
        }

        public void changeTab(int tab) {
            this.tab = tab;
            notifyDataSetChanged();
        }
    }

    public class UpdateEventsView extends ShoutApplication.SendAndReceiveJSON<EventsAdapter
            .ViewHolder> {
        @Override
        public void onPostExecute(Pair<EventsAdapter.ViewHolder, JSONObject> pair) {
            try {
                if (pair.second.get("update").equals("Success!")) {
                    Pair<Integer, Integer> result = ShoutApplication.updateEventInvite(pair.first
                            .eventInvite, getContext(), true);
                    if (result.first.equals(1) && result.second.equals(1)) {
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
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_LONG).show();
            }
        }
    }
}