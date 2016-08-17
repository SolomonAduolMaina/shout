package com.shout;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

public class FcmListenerService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message){
        HashMap<String, String> data = new HashMap<>(message.getData());
    }
}