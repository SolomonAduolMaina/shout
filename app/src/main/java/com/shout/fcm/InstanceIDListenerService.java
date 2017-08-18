package com.shout.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.shout.networkmessaging.SendMessages;

import org.json.JSONException;
import org.json.JSONObject;


public class InstanceIDListenerService extends FirebaseInstanceIdService {
    private final String SEND_TOKEN_PHP_PATH = "http://10.0.2.2/send_message.php";

    @Override
    public void onTokenRefresh() {
        //android.os.Debug.waitForDebugger();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("registration_id", FirebaseInstanceId.getInstance().getToken());
            sendTokenTask(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendTokenTask(final JSONObject jsonObject) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) {
            }
        };
        SendMessages.doOnResponse(lambda, this, jsonObject, SEND_TOKEN_PHP_PATH);
    }
}