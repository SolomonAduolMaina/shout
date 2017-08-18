package com.shout.fragments;

import android.view.View;

import com.shout.R;

public class CreateEventFragment extends SingleEventFragment {
    @Override
    public void setState() {
        STATE = getResources().getInteger(R.integer.create_event);
        createEvent_Button.setText(R.string.create_event);
        createEvent_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEvent(false);
            }
        });
    }
}
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