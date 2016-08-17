package com.shout.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.shout.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsView extends MultiAutoCompleteTextView {
    private Context context;
    private ViewGroup root;
    public ArrayList<String> invitees = new ArrayList<>();

    public FriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public FriendsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void replaceText(CharSequence text) {
        View view = LayoutInflater.from(context).inflate(R.layout.canoncial_text_view, root, false);
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
}