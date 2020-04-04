package com.shout.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.shout.R;
import com.shout.activities.ShoutActivity;
import com.shout.database.ShoutDatabaseDescription.Event;
import com.shout.networkmessaging.SendMessages;
import com.shout.utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MyMapsFragment extends MapFragment implements OnMapReadyCallback {
    final MyMapsFragment THIS_INSTANCE = this;
    GoogleMap googleMap;
    ArrayList<HashMap<String, String>> events;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onResume() {
        super.onResume();
        fetchNearestEvents();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fetchNearestEvents();
    }

    @Override
    public void onPause() {
        super.onPause();
        getFragmentManager().beginTransaction().hide(this).commit();
        Intent intent = new Intent(ShoutActivity.ACTION_FRAGMENT_PAUSED);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void fetchNearestEvents() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", 40.0);
            jsonObject.put("longitude", -75.0);
            jsonObject.put("offset", 0);
            nearestEventsTask(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*if (googleMap != null && mFusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission to access your location not granted!",
                        Toast.LENGTH_LONG).show();
            } else {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("latitude", location.getLatitude());
                                        jsonObject.put("longitude", location.getLongitude());
                                        jsonObject.put("offset", 0);
                                        nearestEventsTask(jsonObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

            }
        }*/
    }

    private void placeMarkers() {
        if (googleMap != null) {
            googleMap.clear();
            double latAverage = 0;
            double longAverage = 0;
            if (events != null) {
                double latitude = 0;
                double longitude = 0;
                int count = 0;
                for (HashMap<String, String> event : events) {
                    String lat = event.get(Event.COLUMN_LATITUDE);
                    String lon = event.get(Event.COLUMN_LONGITUDE);
                    if ((lat != null) && (lon != null)) {
                        latitude = Double.parseDouble(lat);
                        longitude = Double.parseDouble(lon);
                        LatLng position = new LatLng(latitude, longitude);
                        Marker marker =
                                googleMap.addMarker(
                                        new MarkerOptions().
                                                position(position).
                                                title(event.get(Event.COLUMN_TITLE)));
                        marker.setTag(event);
                    }
                    latAverage = latAverage + latitude;
                    longAverage = longAverage + longitude;
                    count = count + 1;
                }
                if (count != 0) {
                    latAverage = latAverage / count;
                    longAverage = longAverage / count;
                }
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latAverage, longAverage)));
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, String> data = (HashMap<String, String>) marker.getTag();
                    getFragmentManager().beginTransaction().hide(THIS_INSTANCE).commit();
                    ShoutActivity.launchViewEventFragment(getFragmentManager(), data);
                }
            });
        }
    }

    void nearestEventsTask(final JSONObject jsonObject) {
        SendMessages.ProcessResponse lambda = new SendMessages.ProcessResponse() {
            @Override
            public void process(JSONObject response) throws JSONException {
                if (response.get("result").equals("Success!")) {
                    JSONArray eventsArray = response.getJSONArray("events");
                    events = new ArrayList<>();
                    for (int index = 0; index < eventsArray.length(); index++) {
                        HashMap<String, String> event =
                                Util.JSONObjectToHashMap(eventsArray.getJSONObject(index));
                        events.add(index, event);
                    }
                    placeMarkers();
                }
            }
        };
        SendMessages.doOnResponse(lambda, getActivity(), jsonObject,
                getString(R.string.nearest_events_path));
    }
}
