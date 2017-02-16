package com.teramatrix.fleetiq.controller;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ...
 * @author Mohsin Khan
 * @date May 16 2016
 */
public class JSONParser {


    public static PolylineOptions[] getPoints(String json) {

        final PolylineOptions options[] = new PolylineOptions[2];
        try {

            options[0] = new PolylineOptions().width(10).color(Color.parseColor("#1c83bf")).geodesic(true);
            options[1] = new PolylineOptions().width(5).color(Color.parseColor("#0bb4fa")).geodesic(true);
            JSONArray array = new JSONArray(json);
            if (array.length() != 0) {
                JSONObject object = array.getJSONObject(0);
                if (object.has("path")) {
                    JSONArray path = object.getJSONArray("path");
                    for (int i = 0; i < path.length(); i++) {
                        JSONObject o = path.getJSONObject(i);
                        LatLng l = new LatLng(Double.parseDouble(o.getString("latitude")), Double.parseDouble(o.getString("longitude")));
                        options[0].add(l);
                        options[1].add(l);
                    }
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return options;
    }
    public static PolylineOptions[] getPoints2(String json){
        final PolylineOptions options[] = new PolylineOptions[2];
        try {
            options[0] = new PolylineOptions().width(10).color(Color.parseColor("#1c83bf")).geodesic(true);
            options[1] = new PolylineOptions().width(5).color(Color.parseColor("#0bb4fa")).geodesic(true);

            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("WorkAllocationTrackingList")) {
                JSONArray jsonArray = jsonObject.getJSONArray("WorkAllocationTrackingList");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject o = jsonArray.getJSONObject(i);
                            LatLng l = new LatLng(Double.parseDouble(o.getString("lat")), Double.parseDouble(o.getString("lng")));
                            options[0].add(l);
                            options[1].add(l);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return options;
    }

    public static LatLng[] getThresholds(String json) {
        try {
            JSONArray array = new JSONArray(json);
            if (array.length() > 0) {
                JSONObject object = array.getJSONObject(0);
                if (object.has("path")) {
                    JSONArray points = object.getJSONArray("points");
                    LatLng[] l = new LatLng[points.length()];
                    for (int i = 0; i < points.length(); i++) {
                        JSONObject o = points.getJSONObject(i);
                        l[i] = new LatLng(Double.parseDouble(o.getString("latitude")), Double.parseDouble(o.getString("longitude")));
                    }
                    return l;
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return new LatLng[]{};
    }
}