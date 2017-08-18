package com.shout.activities;

import android.accounts.Account;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.shout.R;
import com.shout.database.NotificationsProvider;
import com.shout.fragments.CreateEventFragment;
import com.shout.fragments.MyEventsFragment;
import com.shout.fragments.MyMapsFragment;
import com.shout.fragments.SearchFragment;
import com.shout.fragments.ViewEventFragment;
import com.shout.utilities.Util;

import java.util.HashMap;

public class ShoutActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public final static String ACTION_RECEIVED_FCM = "com.shout.ACTION_RECEIVED_FCM";
    public final static String ACTION_FRAGMENT_PAUSED = "com.shout.ACTION_FRAGMENT_PAUSED";

    public static final String VIEW_EVENT_FRAGMENT = "viewEventFragment";
    public static final String MY_EVENTS_FRAGMENT = "mainFragment";
    public static final String MAP_FRAGMENT = "mapFragment";
    public static final String CREATE_FRAGMENT = "createFragment";
    public static final String SEARCH_FRAGMENT = "searchFragment";

    private final AppCompatActivity THIS_INSTANCE = this;
    FloatingActionButton fab;
    FloatingActionButton maps;
    Fragment mainFragment;
    private SearchView searchView;
    private BroadcastReceiver fcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            bundle.putString("user_id", getIntent().getStringExtra("user_id"));
            bundle.putString("notification", "Yes");
            bundle.putString("event_id", intent.getStringExtra("event_id"));
            bundle.putString("type", intent.getStringExtra("type"));
            Account account = getIntent().getParcelableExtra("account");
            ContentResolver.requestSync(account, NotificationsProvider.AUTHORITY, bundle);
        }
    };

    private BroadcastReceiver fragmentPaused = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            redrawCurrentFragment();
        }
    };

    public static void launchViewEventFragment(FragmentManager manager, HashMap<String, String> eventInvite) {
        ViewEventFragment eventFragment = new ViewEventFragment();
        eventFragment.setArguments(Util.HashMapToBundle(eventInvite));
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment, eventFragment, VIEW_EVENT_FRAGMENT);
        transaction.addToBackStack(VIEW_EVENT_FRAGMENT);
        transaction.commit();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fcmReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fragmentPaused);
        super.onPause();
    }

    private void redrawCurrentFragment() {
        String fragmentTag = getFragmentManager().
                getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName();
        Fragment currentFragment = getFragmentManager().findFragmentByTag(fragmentTag);
        if (mainFragment.equals(currentFragment)) {
            maps.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        }
        getFragmentManager().beginTransaction().show(currentFragment).commit();
    }

    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(fcmReceiver,
                new IntentFilter(ACTION_RECEIVED_FCM));
        LocalBroadcastManager.getInstance(this).registerReceiver(fragmentPaused,
                new IntentFilter(ACTION_FRAGMENT_PAUSED));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccessTokenTracker tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken old, AccessToken current) {
                startActivity(new Intent(THIS_INSTANCE, LoginActivity.class));
                THIS_INSTANCE.finish();
            }
        };

        tokenTracker.startTracking();
        setContentView(R.layout.shout_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.create_event);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateEventFragment createEventFragment = new CreateEventFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                maps.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.INVISIBLE);
                transaction.replace(R.id.fragment, createEventFragment, CREATE_FRAGMENT);
                transaction.addToBackStack(CREATE_FRAGMENT);
                transaction.commit();
            }
        });

        maps = (FloatingActionButton) findViewById(R.id.events_map);
        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyMapsFragment mapFragment = new MyMapsFragment();
                mapFragment.getMapAsync(mapFragment);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                getFragmentManager().beginTransaction().hide(mainFragment).commit();
                maps.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.INVISIBLE);
                transaction.add(R.id.map_view, mapFragment, MAP_FRAGMENT);
                transaction.addToBackStack(MAP_FRAGMENT);
                transaction.commit();
            }
        });

        MyEventsFragment eventsFragment = new MyEventsFragment();
        mainFragment = eventsFragment;
        getFragmentManager().
                beginTransaction().
                add(R.id.fragment, eventsFragment, MY_EVENTS_FRAGMENT).
                addToBackStack(MY_EVENTS_FRAGMENT).
                commit();
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
        searchView.setQuery("", false);
        searchView.setIconified(true);

        searchView.clearFocus();

        Bundle args = new Bundle();
        args.putString("query", query);

        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, searchFragment, SEARCH_FRAGMENT);
        transaction.addToBackStack(SEARCH_FRAGMENT);
        transaction.commit();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

}