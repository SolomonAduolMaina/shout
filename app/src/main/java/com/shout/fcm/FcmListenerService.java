package com.shout.fcm;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shout.activities.ShoutActivity;

import java.util.Map;

public class FcmListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> data = message.getData();
        Intent intent = new Intent(ShoutActivity.ACTION_RECEIVED_FCM);
        intent.putExtra("type", data.get("type"));
        intent.putExtra("event_id", data.get("event_id"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}