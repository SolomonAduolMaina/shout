package com.shout.utilities;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Solomon Aduol Maina on 7/24/2017.
 */

public class Util {
    public static Bundle HashMapToBundle(HashMap<String, String> hashMap) {
        Bundle result = new Bundle();
        for (String key : hashMap.keySet()) {
            result.putString(key, hashMap.get(key));
        }
        return result;
    }

    public static HashMap<String, String> BundleToHashMap(Bundle bundle) {
        HashMap<String, String> jsonObject = new HashMap<>();
        for (String key : bundle.keySet()) {
            jsonObject.put(key, bundle.getString(key));
        }
        return jsonObject;
    }

    public static HashMap<String, String> JSONObjectToHashMap(JSONObject jsonObject) throws
            JSONException {
        Iterator<String> keys = jsonObject.keys();
        HashMap<String, String> map = new HashMap<>();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonObject.getString(key));
        }
        return map;
    }
}
