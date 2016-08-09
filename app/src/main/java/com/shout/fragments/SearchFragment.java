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

import com.shout.applications.ShoutApplication.SendAndReceiveJSON;
import com.shout.R;
import com.shout.wrapperClasses.WrapperClasses.*;


import org.json.JSONException;
import org.json.JSONObject;


public class SearchFragment extends Fragment {
    private final String SEARCH_PHP_PATH = "http://shouttestserver.ueuo.com/search.php";
    private final int PERSONS = 0;
    private final int EVENTS = 1;
    private final int GROUPS = 2;
    private RecyclerView resultsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", "'" + getActivity().getIntent().getStringExtra("userId") +
                    "'");
            jsonObject.put("search_query", "'" + getArguments().getString("query") + "'");
            jsonObject.put("offset", "0");
            jsonObject.put("query_type", "All");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        resultsView = (RecyclerView) view.findViewById(R.id.results_recyclerView);
        TabLayout resultsTabs = (TabLayout) view.findViewById(R.id.results_tabLayout);
        resultsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        resultsView.setAdapter(new ResultsAdapter());

        resultsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((ResultsAdapter) resultsView.getAdapter()).changeTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        Pair<Void, JSONObject> pair = new Pair<>(null, jsonObject);
        new SearchTask().execute(new Pair<>(SEARCH_PHP_PATH, pair));

        /*ArrayList<String> phoneNumbersCollection = new ArrayList<>();
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor phoneCursor = contentResolver.query(ContactsContract
                .CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (phoneCursor.moveToNext()) {
            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex
                    (ContactsContract.CommonDataKinds.Phone.DATA));
            phoneNumbersCollection.add(phoneNumber);
        }

        phoneCursor.close();
        String[] emailAddresses = new String[phoneNumbersCollection.size()];
        phoneNumbersCollection.toArray(emailAddresses);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.search_fragment, R.id.friendsAndGroups_textView,
                emailAddresses);
        AutoCompleteTextView textView = (AutoCompleteTextView) view.findViewById(R.id
                .friendsAndGroups_textView);
        textView.setAdapter(adapter);*/
        return view;
    }

    private class SearchTask extends SendAndReceiveJSON<Void> {
        @Override
        protected void onPostExecute(Pair<Void, JSONObject> pair) {
            try {
                ((ResultsAdapter) resultsView.getAdapter()).setData(pair.second);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
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
        public ResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .search_result, parent, false));
        }

        @Override
        public void onBindViewHolder(ResultsAdapter.ViewHolder holder, int position) {
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
