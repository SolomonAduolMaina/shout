package com.shout.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.shout.syncAdapters.NotificationsSyncAdapter;

public class NotificationsService extends Service {
    private static NotificationsSyncAdapter syncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();


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