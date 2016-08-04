package com.shout.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;


import com.shout.R;


public class PreferencesFragment extends PreferenceFragmentCompat {
    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    public static PreferencesFragment newInstance(int sectionNumber) {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
}
