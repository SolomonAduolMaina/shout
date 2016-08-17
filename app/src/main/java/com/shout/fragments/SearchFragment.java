package com.shout.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.shout.applications.ShoutApplication;
import com.shout.applications.ShoutApplication.SearchClasses;
import com.shout.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchFragment extends Fragment {
    private final String SEARCH_PHP_PATH = "http://shouttestserver.ueuo.com/search.php";
    private final int PERSONS = 0;
    private final int EVENTS = 1;
    private final int GROUPS = 2;
    private RecyclerView resultsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        JSONObject jsonObject = new JSONObject();
        String query = getActivity().getIntent().getStringExtra("userId");
        try {
            jsonObject.put("user_id", query);
            jsonObject.put("search_query", getArguments().getString("query"));
            jsonObject.put("offset", "0");
            jsonObject.put("query_type", "All");
            Pair<Void, JSONObject> pair = new Pair<>(null, jsonObject);
            new SearchTask().execute(new Pair<>(SEARCH_PHP_PATH, pair));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((TextView) view.findViewById(R.id.query_textView)).setText("Search results for " + query);
        resultsView = (RecyclerView) view.findViewById(R.id.results_recyclerView);
        TabLayout resultsTabs = (TabLayout) view.findViewById(R.id.results_tabLayout);
        resultsView.setLayoutManager(new LinearLayoutManager(getContext()));
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

    private class SearchTask extends ShoutApplication.SendAndReceiveJSON<Void> {
        @Override
        protected void onPostExecute(Pair<Void, JSONObject> pair) {
            try {
                if (pair.second.getString("insert").equals("Success!")) {
                    ((SearchResultsAdapter) resultsView.getAdapter()).setData(pair.second);
                } else {
                    String remoteError = pair.second.getString("error_message");
                    Toast.makeText(getContext(), remoteError, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter
            .ViewHolder> {
        private int tab = 0;
        public SearchClasses searchClasses;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View view;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
            }
        }

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
    }
}