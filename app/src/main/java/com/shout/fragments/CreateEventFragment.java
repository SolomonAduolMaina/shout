package com.shout.fragments;

import android.view.View;

public class CreateEventFragment extends SingleEventFragment {
    @Override
    public void configureButton() {
        createEvent_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEvent(true, false);
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