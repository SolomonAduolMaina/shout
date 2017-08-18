package com.shout.fragments;

import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import com.shout.R;
import com.shout.activities.ShoutActivity;
import com.shout.database.DatabaseUtilities;
import com.shout.database.NotificationsProvider;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.database.ShoutDatabaseDescription.Invite;

public class MyEventsFragment extends EventsListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    String userId;

    private BroadcastReceiver eventsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor data = getActivity().getContentResolver().query(NotificationsProvider
                    .NOTIFICATIONS_URI, null, "Event.creator_id = ? OR Invite" +
                    ".invitee_id = ?", new String[]{userId, userId}, null);
            ((EventsAdapter) eventsRecyclerView.getAdapter()).setData(data, userId);

            if (intent.getStringExtra("notification").equals("Yes")) {
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(getActivity())
                                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                .setContentTitle(intent.getStringExtra("type"))
                                .setContentText(intent.getStringExtra("From " +
                                        intent.getStringExtra("event_id")));
                Notification notification = notificationBuilder.build();

                Intent resultIntent = new Intent(getActivity(), ShoutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
                stackBuilder.addParentStack(ShoutActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager = (NotificationManager)
                        getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);
                Cursor event = getActivity().getContentResolver().query
                        (NotificationsProvider.NOTIFICATIONS_URI, null,
                                "(Event.creator_id = ? OR Invite.invitee_id = ?) AND Event.event_id = ?",
                                new String[]{userId, userId, intent.getStringExtra("event_id")}, null);
                ShoutActivity.launchViewEventFragment(getFragmentManager(),
                        DatabaseUtilities.eventInvite(event));
            }
        }
    };

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(eventsReceiver,
                new IntentFilter(ACTION_UPDATED_DATABASE));
        getLoaderManager().getLoader(0).forceLoad();
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(eventsReceiver);
        super.onPause();
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
        ((EventsAdapter) eventsRecyclerView.getAdapter()).setData((Cursor) null, userId);
    }
}