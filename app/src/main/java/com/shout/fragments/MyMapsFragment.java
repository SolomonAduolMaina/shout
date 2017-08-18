package com.shout.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shout.activities.ShoutActivity;
import com.shout.database.NotificationsProvider;
import com.shout.database.ShoutDatabaseDescription.Event;

import java.util.HashMap;

import static com.shout.database.DatabaseUtilities.eventInvite;

public class MyMapsFragment extends MapFragment implements OnMapReadyCallback {
    final MyMapsFragment THIS_INSTANCE = this;
    GoogleMap googleMap;

    @Override
    public void onResume() {
        super.onResume();
        if (googleMap != null) {
            placeMarkers();
        }
    }

    private void placeMarkers() {
        googleMap.clear();
        String userId = getActivity().getIntent().getStringExtra("user_id");
        Cursor data = getActivity().getContentResolver().query(NotificationsProvider
                .NOTIFICATIONS_URI, null, "Event.creator_id = ? OR Invite" +
                ".invitee_id = ?", new String[]{userId, userId}, null);
        double latAverage = 0;
        double longAverage = 0;
        if (data != null) {
            double latitude = 0;
            double longitude = 0;
            int count = 0;
            while (data.moveToNext()) {
                String lat = data.getString(data.getColumnIndex(Event.COLUMN_LATITUDE));
                String lon = data.getString(data.getColumnIndex(Event.COLUMN_LONGITUDE));
                if ((lat != null) && (lon != null)) {
                    latitude = Double.parseDouble(lat);
                    longitude = Double.parseDouble(lon);
                    LatLng position = new LatLng(latitude, longitude);
                    Marker marker = googleMap.addMarker(
                            new MarkerOptions().
                                    position(position).
                                    title(data.getString(data.getColumnIndex(Event.COLUMN_TITLE))));
                    marker.setTag(eventInvite(data));
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        placeMarkers();
    }

    @Override
    public void onPause() {
        super.onPause();
        getFragmentManager().beginTransaction().hide(this).commit();
        Intent intent = new Intent(ShoutActivity.ACTION_FRAGMENT_PAUSED);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
