package com.shout.fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.shout.R;
import com.shout.database.DatabaseUtilities;
import com.shout.database.ShoutDatabaseDescription;
import com.shout.networkmessaging.SendMessages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.shout.utilities.Util.JSONObjectToHashMap;


public abstract class EventsListFragment extends Fragment {
    public final static String ACTION_UPDATED_DATABASE = "com.shout.ACTION_UPDATED_DATABASE";
    final static int ALL = 0;
    final static int MY_EVENTS = 1;
    final static int INVITED = 2;
    final static int SUGGESTED = 3;

    RecyclerView eventsRecyclerView;
    TabLayout eventsTabLayout;

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
            public void onTabSelected(TabLayout.Tab tab) {
                ((EventsListFragment.EventsAdapter) eventsRecyclerView.getAdapter())
                        .changeTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return rootView;
    }

    void updateGoingTask(final Pair<EventsAdapter.ViewHolder, JSONObject> pair) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) throws JSONException {
                if (response.get("result").equals("Success!")) {
                    HashMap<String, String> newData =
                            JSONObjectToHashMap(response.getJSONObject("event_invite"));
                    Pair<Integer, Integer> result = DatabaseUtilities.updateLocalDatabase(newData,
                            getActivity(), true);
                    if (result.first.equals(1) && result.second.equals(1)) {
                        EventsAdapter adapter = (EventsAdapter) eventsRecyclerView.getAdapter();
                        pair.first.updateData(newData);
                        HashMap<String, String> oldData = pair.first.data;
                        int position;
                        switch (adapter.tab) {
                            case ALL:
                                position = adapter.eventInviteClasses.all.indexOf(oldData);
                                adapter.eventInviteClasses.all.set(position, newData);
                                position = adapter.eventInviteClasses.userEvents.indexOf(oldData);
                                if (position > -1) {
                                    adapter.eventInviteClasses.userEvents.set(position, newData);
                                } else {
                                    position = adapter.eventInviteClasses.invited.indexOf(oldData);
                                    if (position > -1) {
                                        adapter.eventInviteClasses.invited.set(position, newData);
                                    } else {
                                        position = adapter.eventInviteClasses.suggested.indexOf(oldData);
                                        adapter.eventInviteClasses.suggested.set(position, newData);
                                    }
                                }
                                break;
                            case MY_EVENTS:
                                position = adapter.eventInviteClasses.all.indexOf(oldData);
                                adapter.eventInviteClasses.all.set(position, newData);
                                position = adapter.eventInviteClasses.userEvents.indexOf(oldData);
                                adapter.eventInviteClasses.userEvents.set(position, newData);
                                break;
                            case INVITED:
                                position = adapter.eventInviteClasses.all.indexOf(oldData);
                                adapter.eventInviteClasses.all.set(position, newData);
                                position = adapter.eventInviteClasses.invited.indexOf(oldData);
                                adapter.eventInviteClasses.invited.set(position, newData);
                                break;
                            case SUGGESTED:
                                position = adapter.eventInviteClasses.all.indexOf(oldData);
                                adapter.eventInviteClasses.all.set(position, newData);
                                position = adapter.eventInviteClasses.suggested.indexOf(oldData);
                                adapter.eventInviteClasses.suggested.set(position, newData);
                                break;
                        }
                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
                    } else {
                        String localError = "Error updating local database";
                        Toast.makeText(getActivity(), localError, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        SendMessages.doOnResponse(lambda, getActivity(), pair.second,
                getString(R.string.update_going_php_path));
    }

    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
        DatabaseUtilities.EventInviteClasses eventInviteClasses;
        int tab = 0;

        EventsAdapter() {
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
                        data = eventInviteClasses.userEvents;
                        break;
                    case INVITED:
                        data = eventInviteClasses.invited;
                        break;
                    case SUGGESTED:
                        data = eventInviteClasses.suggested;
                        break;
                }
                holder.setEventsViewHolder(position, data);

            }
        }

        @Override
        public int getItemCount() {
            if (eventInviteClasses != null) {
                switch (tab) {
                    case ALL:
                        return eventInviteClasses.all.size();
                    case MY_EVENTS:
                        return eventInviteClasses.userEvents.size();
                    case INVITED:
                        return eventInviteClasses.invited.size();
                    case SUGGESTED:
                        return eventInviteClasses.suggested.size();
                }
            }
            return 0;
        }

        void setData(Cursor cursor, String userId) {
            eventInviteClasses = new DatabaseUtilities.EventInviteClasses(cursor, userId);
            notifyDataSetChanged();
        }

        void setData(JSONArray data, String userId) throws JSONException {
            eventInviteClasses = new DatabaseUtilities.EventInviteClasses(data, userId);
            notifyDataSetChanged();
        }

        void changeTab(int tab) {
            this.tab = tab;
            notifyDataSetChanged();
        }

        class ViewHolder extends DatabaseUtilities.EventsViewHolder {
            ViewHolder(View view) {
                super(view, getFragmentManager());
            }

            View.OnClickListener setUpdateInviteListener(final ViewHolder holder,
                                                         final String goingValue) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.data.put(ShoutDatabaseDescription.Invite.COLUMN_GOING, goingValue);
                        JSONObject jsonObject = new JSONObject(holder.data);
                        updateGoingTask(new Pair<>(holder, jsonObject));
                    }
                };
            }

            @Override
            public void setYesOnClickListener(Button button) {
                String userId = getActivity().getIntent().getStringExtra("user_id");
                if (this.data.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID).equals(userId)) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setOnClickListener(setUpdateInviteListener(this, "Yes"));
                }
            }

            @Override
            public void setNoOnClickListener(Button button) {
                String userId = getActivity().getIntent().getStringExtra("user_id");
                if (this.data.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID).equals(userId)) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setOnClickListener(setUpdateInviteListener(this, "No"));
                }
            }
        }
    }
}
