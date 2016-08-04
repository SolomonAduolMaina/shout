package com.shout.activities;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.shout.fragments.CreateEventFragment;
import com.shout.fragments.SearchFragment;
import com.shout.fragments.EventsFragment;
import com.shout.R;
import com.shout.fragments.NotificationsFragment;
import com.shout.fragments.PreferencesFragment;
import com.shout.notificationsProvider.NotificationsProvider;

public class ShoutActivity extends AppCompatActivity {
    public final static String ACTION_FINISHED_SYNC = "com.shout.ACTION_FINISHED_SYNC";
    public static IntentFilter syncIntentFilter = new IntentFilter(ACTION_FINISHED_SYNC);

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private AccessTokenTracker tokenTracker;
    private final AppCompatActivity THIS_INSTANCE = this;
    private ViewPager mViewPager;



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

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putString("userId", "'" + getIntent().getStringExtra("userId") + "'");
        Account account = new Account("dummy account", "http://shouttestserver.ueuo.com");
        AccountManager accountManager = (AccountManager) this.getSystemService(ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(account, null, null);
        //ContentResolver.setSyncAutomatically(account, ShoutDatabaseDescription.AUTHORITY, true);
        ContentResolver.requestSync(account, NotificationsProvider.AUTHORITY, settingsBundle);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(2);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return PreferencesFragment.newInstance(0);
                case 1:
                    return NotificationsFragment.newInstance(1);
                case 2:
                    return EventsFragment.newInstance(2);
                case 3:
                    return CreateEventFragment.newInstance(3);
                default:
                    return SearchFragment.newInstance(4);
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence result = null;
            switch (position) {
                case 0:
                    return "Settings And Preferences";
                case 1:
                    return "Notifications";
                case 2:
                    return "My Events";
                case 3:
                    return "Create Event";
                default:
                    return "Friends And Groups";
            }
        }
    }
}