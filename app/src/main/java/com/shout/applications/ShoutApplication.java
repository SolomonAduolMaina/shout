package com.shout.applications;

import android.app.Application;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.shout.notificationsProvider.NotificationsProvider;
import com.shout.notificationsProvider.ShoutDatabaseDescription;
import com.shout.wrapperClasses.WrapperClasses;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;

public class ShoutApplication extends Application {
    // Updated your class body:
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Facebook SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }


    public static String readInputStream(InputStream inputStream) {
        if (inputStream != null) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    public static abstract class SendAndReceiveJSON<Input> extends AsyncTask<Pair<String,
            Pair<Input, JSONObject>>, Void, Pair<Input, JSONObject>> {

        @Override
        protected Pair<Input, JSONObject> doInBackground(Pair<String, Pair<Input, JSONObject>>...
                                                                         args) {
            Pair<String, JSONObject> pair = new Pair<>(args[0].first, args[0].second.second);
            return (new Pair<Input, JSONObject>(args[0].second.first, getJsonResponse(pair)));
        }


        @Override
        protected abstract void onPostExecute(Pair<Input, JSONObject> pair);
    }

    @NonNull
    public static  JSONObject getJsonResponse(Pair<String, JSONObject> pair) {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        JSONObject response = new JSONObject();
        try {
            URL url = new URL(pair.first);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            String message = pair.second.toString();
            connection.setFixedLengthStreamingMode(message.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.connect();
            outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(message.getBytes());
            outputStream.flush();
            inputStream = connection.getInputStream();
            inputStream = new BufferedInputStream(connection.getInputStream());
            response = new JSONObject(readInputStream(inputStream));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
