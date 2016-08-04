package com.shout.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.shout.R;


public class FriendsView extends MultiAutoCompleteTextView {

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

    @Override
    public void replaceText(CharSequence text) {
        View view = LayoutInflater.from(context).inflate(R.layout.test_text_view, root, false);
        TextView textView = (TextView) view.findViewById(R.id.content_textView);
        textView.setText(text.toString());
        int childCount = root.getChildCount();
        if (childCount == 1) {
            root.addView(view, 0);
        } else {
            root.addView(view, childCount - 1);
        }
        this.setText("");
    }

    public void setRoot(ViewGroup root) {
        this.root = root;
    }
}