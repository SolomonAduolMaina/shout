package com.shout.applications;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;

public class ShoutApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppEventsLogger.activateApp(this);
    }
}