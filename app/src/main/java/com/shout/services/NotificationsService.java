package com.shout.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.shout.syncAdapter.NotificationsSyncAdapter;

public class NotificationsService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static NotificationsSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new NotificationsSyncAdapter(getApplicationContext(), true, false);
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}