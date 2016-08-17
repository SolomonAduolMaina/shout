package com.shout.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.shout.R;
import com.shout.fragments.CreateEventFragment;
import com.shout.fragments.MyEventsFragment;
import com.shout.fragments.SearchFragment;

public class ShoutActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public final static String ACTION_FINISHED_SYNC = "com.shout.ACTION_FINISHED_SYNC";
    public final static IntentFilter syncIntentFilter = new IntentFilter(ACTION_FINISHED_SYNC);
    private AccessTokenTracker tokenTracker;
    private final AppCompatActivity THIS_INSTANCE = this;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken old, AccessToken current) {
                startActivity(new Intent(THIS_INSTANCE, LoginActivity.class));
                THIS_INSTANCE.finish();
            }
        };

        setContentView(R.layout.shout_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateEventFragment createEventFragment = new CreateEventFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment, createEventFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        MyEventsFragment eventsFragment = new MyEventsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, eventsFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        SearchFragment searchFragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString("query", query);
        searchFragment.setArguments(args);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService
                (Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, searchFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }
}