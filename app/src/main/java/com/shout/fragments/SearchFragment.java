package com.shout.fragments;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.shout.R;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public SearchFragment() {
    }

    public static SearchFragment newInstance(int sectionNumber) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        ArrayList<String> phoneNumbersCollection = new ArrayList<>();
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
        textView.setAdapter(adapter);
        return view;
    }
}
