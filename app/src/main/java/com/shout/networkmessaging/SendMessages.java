package com.shout.networkmessaging;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class SendMessages {
    private static SendMessages instance;
    private static Context context;
    private RequestQueue requestQueue;

    private SendMessages(Context ctx) {
        context = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized SendMessages getInstance(Context context) {
        if (instance == null) {
            instance = new SendMessages(context);
        }
        return instance;
    }

    public static void doOnResponse(final ProcessResponse f,
                                    Context c, JSONObject jsonObject, String url) {
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        f.process(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Handle Network Error
                    }
                });
        SendMessages.getInstance(c).addToRequestQueue(request);
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public interface ProcessResponse {
        void process(JSONObject response);
    }
}