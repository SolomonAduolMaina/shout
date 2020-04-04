package com.shout.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
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
import com.shout.database.DatabaseUtilities;
import com.shout.database.DatabaseUtilities.SearchClasses;
import com.shout.database.ShoutDatabaseDescription;
import com.shout.networkmessaging.SendMessages;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.shout.activities.ShoutActivity.ACTION_FRAGMENT_PAUSED;
import static com.shout.fragments.PersonEventsFragment.PERSON_EVENTS_FRAGMENT;
import static com.shout.utilities.Util.JSONObjectToHashMap;

public class SearchFragment extends Fragment {
    private final int PERSONS = 0;
    private final int EVENTS = 1;
    private final int GROUPS = 2;

    private RecyclerView resultsView;
    private String userId;

    private int OFFSET = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        String query = getArguments().getString("query");
        String message = "Search results for " + query;
        ((TextView) view.findViewById(R.id.query_textView)).setText(message);

        userId = getActivity().getIntent().getStringExtra("user_id");
        resultsView = (RecyclerView) view.findViewById(R.id.results_recyclerView);
        TabLayout resultsTabs = (TabLayout) view.findViewById(R.id.results_tabLayout);
        resultsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        resultsView.setAdapter(new SearchResultsAdapter());

        resultsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((SearchResultsAdapter) resultsView.getAdapter()).changeTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", getActivity().getIntent().getStringExtra("user_id"));
            jsonObject.put("search_query", query);
            jsonObject.put("offset", OFFSET);
            searchTask(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent(ACTION_FRAGMENT_PAUSED);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    public void searchTask(final JSONObject jsonObject) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) throws JSONException {
                if (response.getString("result").equals("Success!")) {
                    ((SearchResultsAdapter) resultsView.getAdapter()).setData(response);
                } else {
                    String remoteError = response.getString("error_message");
                    Toast.makeText(getActivity(), remoteError, Toast.LENGTH_LONG).show();
                }
            }
        };
        SendMessages.doOnResponse(lambda, getActivity(), jsonObject, getString(R.string.search_php_path));
    }

    void updateGoingTask(final Pair<SearchResultsAdapter.ViewHolder, JSONObject> pair) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) throws JSONException {
                if (response.get("result").equals("Success!")) {
                    HashMap<String, String> newData =
                            JSONObjectToHashMap(response.getJSONObject("event_invite"));
                    Pair<Integer, Integer> result = DatabaseUtilities.updateLocalDatabase(newData,
                            getActivity(), true);
                    if (result.first.equals(1) && result.second.equals(1)) {
                        SearchResultsAdapter adapter = (SearchResultsAdapter) resultsView.getAdapter();
                        HashMap<String, String> oldData = pair.first.data;
                        pair.first.updateData(newData);
                        int position = adapter.searchClasses.events.indexOf(oldData);
                        adapter.searchClasses.events.set(position, newData);

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

    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
        SearchClasses searchClasses;
        private int tab = 0;

        @Override
        public SearchResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .search_result, parent, false));
        }

        @Override
        public void onBindViewHolder(SearchResultsAdapter.ViewHolder holder, int position) {
            if (searchClasses != null) {
                switch (tab) {
                    case EVENTS:
                        holder.setEventsViewHolder(position, searchClasses.events);
                        holder.view.findViewById(R.id.events_cardView).setVisibility(View.VISIBLE);
                        holder.view.findViewById(R.id.group_cardView).setVisibility(View.GONE);
                        holder.view.findViewById(R.id.person_cardView).setVisibility(View.GONE);
                        break;
                    case PERSONS:
                        holder.view.findViewById(R.id.events_cardView).setVisibility(View.GONE);
                        holder.view.findViewById(R.id.group_cardView).setVisibility(View.GONE);
                        holder.view.findViewById(R.id.person_cardView).setVisibility(View.VISIBLE);

                        TextView name = (TextView) holder.view.findViewById(R.id.person_textView);
                        final HashMap<String, String> person = searchClasses.people.get(position);
                        name.setText(person.get("user_name"));

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PersonEventsFragment personEventsFragment = new PersonEventsFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("person_id", person.get("user_id"));
                                personEventsFragment.setArguments(bundle);
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment, personEventsFragment, PERSON_EVENTS_FRAGMENT);
                                transaction.addToBackStack(PERSON_EVENTS_FRAGMENT);
                                transaction.commit();
                            }
                        });
                        break;
                    case GROUPS:
                        holder.view.findViewById(R.id.events_cardView).setVisibility(View.GONE);
                        holder.view.findViewById(R.id.group_cardView).setVisibility(View.VISIBLE);
                        holder.view.findViewById(R.id.person_cardView).setVisibility(View.GONE);

                        name = (TextView) holder.view.findViewById(R.id.group_textView);
                        HashMap<String, String> group = searchClasses.groups.get(position);
                        name.setText(group.get("group_name"));
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (searchClasses != null) {
                switch (tab) {
                    case EVENTS:
                        return searchClasses.events.size();
                    case PERSONS:
                        return searchClasses.people.size();
                    case GROUPS:
                        return searchClasses.groups.size();
                }
            }
            return 0;
        }

        public void setData(JSONObject jsonObject) throws JSONException {
            this.searchClasses = new SearchClasses(jsonObject);
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
                        holder.data.put(ShoutDatabaseDescription.Invite.COLUMN_INVITEE_ID, userId);
                        holder.data.put(ShoutDatabaseDescription.Invite.COLUMN_GOING, goingValue);
                        if (holder.data.get(ShoutDatabaseDescription.Invite.COLUMN_TYPE).equals("null")) {
                            holder.data.put(ShoutDatabaseDescription.Invite.COLUMN_TYPE, "Joined");
                            holder.data.put(ShoutDatabaseDescription.Invite.COLUMN_SENT, "Yes");
                        }
                        JSONObject jsonObject = new JSONObject(holder.data);
                        updateGoingTask(new Pair<>(holder, jsonObject));
                    }
                };
            }

            @Override
            public void setYesOnClickListener(Button button) {
                if (this.data.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID).equals(userId)) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setOnClickListener(setUpdateInviteListener(this, "Yes"));
                }
            }

            @Override
            public void setNoOnClickListener(Button button) {
                if (this.data.get(ShoutDatabaseDescription.Event.COLUMN_CREATOR_ID).equals(userId)) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setOnClickListener(setUpdateInviteListener(this, "No"));
                }
            }
        }
    }
}