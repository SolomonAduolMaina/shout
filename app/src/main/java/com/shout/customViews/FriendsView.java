package com.shout.customViews;

import android.content.Context;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.shout.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsView extends AppCompatMultiAutoCompleteTextView {
    public ArrayList<String> invitees = new ArrayList<>();
    private Context context;
    private ViewGroup root;

    public FriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

    }

    public FriendsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setFriendsAdapter(Context context, ArrayList<Pair<String, String>> pairs) {
        this.setAdapter(new FriendsAdapter(context, pairs));
    }

    @Override
    public void replaceText(CharSequence text) {
        View view = LayoutInflater.from(context).inflate(R.layout.canonical_text_view, root, false);
        try {
            JSONObject data = new JSONObject(text.toString());
            ((TextView) view.findViewById(R.id.content_textView)).setText(data.optString("name"));
            int childCount = root.getChildCount();
            if (childCount == 1) {
                root.addView(view, 0);
            } else {
                root.addView(view, childCount - 1);
            }
            this.setText("");
            invitees.add(data.optString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRoot(ViewGroup root) {
        this.root = root;
    }

    private static class FriendsAdapter extends BaseAdapter implements Filterable {
        private Context context;
        private ArrayList<Pair<String, String>> originalData;
        private ArrayList<Pair<String, String>> filteredData;
        private FriendsFilter friendsFilter;

        FriendsAdapter(Context context, ArrayList<Pair<String, String>> originalData) {
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
            if (filteredData == null) {
                return null;
            } else {
                JSONObject pair = new JSONObject();
                try {
                    pair.put("id", filteredData.get(i).first);
                    pair.put("name", filteredData.get(i).second);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return pair;
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.canonical_text_view,
                        parent, false);
                holder = new ViewHolder();
                holder.view = convertView.findViewById(R.id.content_textView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Pair<String, String> pair = filteredData.get(position);
            TextView textView = (TextView) holder.view.findViewById(R.id.content_textView);
            textView.setText(pair.second);
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return friendsFilter;
        }

        class ViewHolder {
            View view;
        }

        class FriendsFilter extends Filter {
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
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList<Pair<String, String>>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }
}