package com.shout.fragments;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shout.activities.ShoutActivity;
import com.shout.database.NotificationsProvider;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;

public class MyEventsFragment extends EventsListFragment implements LoaderManager
        .LoaderCallbacks<Cursor> {
    private BroadcastReceiver eventsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor data = getActivity().getContentResolver().query(NotificationsProvider
                            .NOTIFICATIONS_URI, null, "Event.creator_id = ? OR Invite" +
                            ".invitee_id = ?", new String[]{userId, userId}, null);
            ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(data, userId);
        }
    };

    @Override
    public void onResume() {
        getFragmentManager().beginTransaction().show(this).commit();
        getActivity().registerReceiver(eventsReceiver, ShoutActivity.syncIntentFilter);
        getLoaderManager().getLoader(0).forceLoad();
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(eventsReceiver);
        super.onPause();
    }

    @Override
    public void setYesOnClickListener(Button button, EventsAdapter.ViewHolder holder) {
        if (holder.eventInvite.get(Event.COLUMN_CREATOR_ID).equals(userId)) {
            button.setVisibility(View.GONE);
        } else {
            button.setOnClickListener(setUpdateInviteListener(holder, "Yes"));
        }
    }

    @Override
    public void setNoOnClickListener(Button button, EventsAdapter.ViewHolder holder) {
        if (holder.eventInvite.get(Event.COLUMN_CREATOR_ID).equals(userId)) {
            button.setVisibility(View.GONE);
        } else {
            button.setOnClickListener(setUpdateInviteListener(holder, "No"));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getActivity().getIntent().getStringExtra("user_id");
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), NotificationsProvider.NOTIFICATIONS_URI, null,
                Event.COLUMN_CREATOR_ID + " = ? OR " + Invite.COLUMN_INVITEE_ID + " = ?", new
                String[]{userId, userId}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(data, userId);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(null, userId);
    }
}