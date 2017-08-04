package com.shout.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.shout.R;
import com.shout.database.DatabaseUtilities.SearchClasses;
import com.shout.networkmessaging.SendMessages;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchFragment extends Fragment {
    private final int PERSONS = 0;
    private final int EVENTS = 1;
    private final int GROUPS = 2;
    private RecyclerView resultsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        JSONObject jsonObject = new JSONObject();
        String query = getActivity().getIntent().getStringExtra("user_id");
        try {
            jsonObject.put("user_id", query);
            jsonObject.put("search_query", getArguments().getString("query"));
            jsonObject.put("offset", "0");
            jsonObject.put("query_type", "All");
            searchTask(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((TextView) view.findViewById(R.id.query_textView)).setText("Search results for " + query);
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
        return view;
    }

    public void searchTask(final JSONObject jsonObject) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) {
                try {
                    if (response.getString("insert").equals("Success!")) {
                        ((SearchResultsAdapter) resultsView.getAdapter()).setData(response);
                    } else {
                        String remoteError = response.getString("error_message");
                        Toast.makeText(getActivity(), remoteError, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        SendMessages.doOnResponse(lambda, getActivity(), jsonObject, getString(R.string.search_php_path));
    }


    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter
            .ViewHolder> {
        public SearchClasses searchClasses;
        private int tab = 0;

        @Override
        public SearchResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .search_result, parent, false));
        }

        @Override
        public void onBindViewHolder(SearchResultsAdapter.ViewHolder holder, int position) {
            if (searchClasses != null) {
                try {
                    TextView name = (TextView) holder.view.findViewById(R.id.name_textView);
                    TextView type = (TextView) holder.view.findViewById(R.id.type_textView);
                    JSONObject data;
                    switch (tab) {
                        case EVENTS:
                            data = searchClasses.eventInvites.getJSONObject(position);
                            name.setText(data.getString("title"));
                            type.setText("Event");
                            holder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            });
                            break;
                        case PERSONS:
                            data = searchClasses.persons.getJSONObject(position);
                            name.setText(data.getString("user_name"));
                            type.setText("People");
                            break;
                        case GROUPS:
                            data = searchClasses.groups.getJSONObject(position);
                            name.setText(data.getString("group_name"));
                            type.setText("Group");
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            if (searchClasses != null) {
                switch (tab) {
                    case EVENTS:
                        return searchClasses.eventInvites.length();
                    case PERSONS:
                        return searchClasses.persons.length();
                    case GROUPS:
                        return searchClasses.groups.length();
                }
            }
            return 0;
        }

        public void setData(JSONObject jsonObject) throws JSONException {
            this.searchClasses = new SearchClasses(jsonObject);
            notifyDataSetChanged();
        }

        public void changeTab(int tab) {
            this.tab = tab;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View view;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
            }
        }
    }
}