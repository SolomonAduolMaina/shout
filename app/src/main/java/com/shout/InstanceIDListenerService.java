package com.shout;

import android.util.Log;
import android.util.Pair;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.shout.applications.ShoutApplication;

import org.json.JSONException;
import org.json.JSONObject;

public class InstanceIDListenerService extends FirebaseInstanceIdService {
    private final String SEND_TOKEN_PHP_PATH = "http://shouttestserver.ueuo.com/send_message.php";
    @Override
    public void onTokenRefresh() {
        //android.os.Debug.waitForDebugger();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("registrationId", FirebaseInstanceId.getInstance().getToken());
            Pair<String, JSONObject> pair = new Pair<>(SEND_TOKEN_PHP_PATH, jsonObject);
            JSONObject result = ShoutApplication.getJsonResponse(pair);
            Log.v("MyTag", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}