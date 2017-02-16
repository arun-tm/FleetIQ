package com.teramatrix.fleetiq.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.Interface.IRefreshFragment;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.SPUtils;
import com.teramatrix.fleetiq.controller.RESTClient;
import com.teramatrix.fleetiq.model.LocationPoints;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import in.teramatrix.googleservices.model.Distance;
import in.teramatrix.googleservices.model.TravelMode;
import in.teramatrix.googleservices.service.DistanceCalculator;
import in.teramatrix.googleservices.service.RouteDesigner;
import in.teramatrix.slidingpanel.SlidingUpPanelLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class MapFragment extends Fragment implements GoogleMap.OnMapLoadedCallback, IRefreshFragment, DatePickerDialog.OnDateSetListener,OnMapReadyCallback {


    private GoogleMap map;
    double latitude = 26.917;
    double longitude = 75.817;
    Snackbar snackbar;
    private AlertDialog dialog;
    private Call call;
    private TextView txtFromDate;
    private TextView txtToDate;
    private Date to = null;
    private Date from = null;
    SimpleDateFormat formatWithoutTime = new SimpleDateFormat("dd MMM yyyy");
    SimpleDateFormat formatWithTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
    private static final String prefixTo = "To_Date";
    private static final String prefixFrom = "From_Date";
    private SlidingUpPanelLayout panelLayout;
    private ImageView indicator;
    private ArrayList<ArrayList<LocationPoints>> routeList = new ArrayList<ArrayList<LocationPoints>>();
    ArrayList<Polyline[]> routePolyline = new ArrayList<Polyline[]>();
    private DonutProgress donutProgressengine_load_indicator;
    private View convertView;
    Marker sourceMarker, dstinationMarker;

    String eachTripDistance[];

    private TripAddapter tripAddapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_map2, null, false);

            View view = container.findViewById(R.id.linerlayout_daterange);

            initializePanel();

            dialog = new SpotsDialog(getActivity(), R.style.Custom);
            dialog.setCancelable(false);


            if (new GeneralUtilities(getActivity()).isConnected())
                initializeMap(savedInstanceState);

            initViews();
        }

        return convertView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
        if (dialog != null)
            dialog.dismiss();
        if (call != null)
            call.cancel();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        try {
            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.action_graph).setVisible(true);
            super.onPrepareOptionsMenu(menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_graph:
                // app icon in action bar clicked; goto parent activity.
                CustomChartFragment customChartFragment = new CustomChartFragment();
                GeneralUtilities.loadFragment((AppCompatActivity) getActivity(), customChartFragment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        try {

            GeneralUtilities.setUpActionBar(getActivity(), this, new SPUtils(getActivity()).getVehicleModel() + " Trips", null);

            txtFromDate = (TextView) convertView.findViewById(R.id.txt_from_date);
            txtToDate = (TextView) convertView.findViewById(R.id.txt_to_date);

            txtFromDate.setText(GeneralUtilities.getDateTime("IST", "d MMM yyyy", -1));
            txtToDate.setText(GeneralUtilities.getDateTime("IST", "d MMM yyyy", 0));


            txtFromDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPicker(prefixFrom, txtFromDate.getText().toString());
                }
            });
            txtToDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPicker(prefixTo, txtToDate.getText().toString());
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapLoaded() {
        try {
            if (new GeneralUtilities(getActivity()).isConnected()) {
                loadHistoryTrackingData(txtFromDate.getText().toString() + " 00:00:00", txtToDate.getText().toString() + " 23:59:59");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void initializePanel() {

        try {

            indicator = (ImageView) convertView.findViewById(R.id.indicator);

            donutProgressengine_load_indicator = (DonutProgress) convertView.findViewById(R.id.engine_load_indicator);
            donutProgressengine_load_indicator.setMax(100);
            donutProgressengine_load_indicator.setProgress(0);
            donutProgressengine_load_indicator.setTextSize(40);
            donutProgressengine_load_indicator.setTextColor(Color.WHITE);
            donutProgressengine_load_indicator.setSuffixText("");
            donutProgressengine_load_indicator.setFinishedStrokeColor(getResources().getColor(R.color.green));
            donutProgressengine_load_indicator.setFinishedStrokeWidth(10);
            donutProgressengine_load_indicator.setUnfinishedStrokeWidth(10);


            panelLayout = (SlidingUpPanelLayout) convertView.findViewById(R.id.sliding_layout);
            panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    //Log.e(TAG, "onPanelSlide, offset " + slideOffset);
                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                    try {
                        indicator.setImageResource(
                                (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                                        ? R.mipmap.ic_arrow_drop_up_white_24dp
                                        : R.mipmap.ic_arrow_drop_down_white_24dp);

                        setLocationOnMap();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            panelLayout.setFadeOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void initializeMap(Bundle savedInstanceState) {
        try {
            //Obtaining our MapView defined in xml
            final MapView mapView = (MapView) convertView.findViewById(R.id.mapView);


            if(savedInstanceState==null)
            {
                System.out.println("savedInstanceState null");
            }

            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            MapsInitializer.initialize(getActivity());
            mapView.getMapAsync(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void refreshFragment() {
        onMapLoaded();
    }

    private void setLocationOnMap() {
        try {
            // create marker
            double lat = Double.parseDouble(HomeFragment.obdData.get("latitude").get(HomeFragment.PARAM_VALUE));
            double lng = Double.parseDouble(HomeFragment.obdData.get("longitude").get(HomeFragment.PARAM_VALUE));
            MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_vehical_marker));
            // adding marker
            map.addMarker(marker);
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }

    }

    private void loadHistoryTrackingData(String from_date, String to_date) {
        try {

            dialog.show();
            routeList.clear();

            final ArrayList<PolylineOptions[]> polylineOptionsesList = new ArrayList<PolylineOptions[]>();


            final PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.parseColor("#546E7A")).geodesic(true);
            final PolylineOptions polylineOptions2 = new PolylineOptions().width(5).color(Color.parseColor("#90A4AE")).geodesic(true);

            SPUtils spUtils = new SPUtils(getActivity());


            from_date = GeneralUtilities.format_DateString_From_One_Pattern_To_Another_Pattern(from_date, "dd MMM yyyy hh:mm:ss", "yyyy-MM-dd");
            to_date = GeneralUtilities.format_DateString_From_One_Pattern_To_Another_Pattern(to_date, "dd MMM yyyy hh:mm:ss", "yyyy-MM-dd");

            from_date = from_date + " 00:00:00";
            to_date = to_date + " 23:59:59";

            String body = "token=" + spUtils.getToken() +
                    "&deviceid=" + spUtils.getDeviceID() +
                    "&service_name=" + "" +
                    "&data_source=" + "" +
                    "&userkey=" + spUtils.getUserKey() +
                    "&user_id=" + spUtils.getUserID() +
                    "&fromdate=" + from_date +
                    "&enddate=" + to_date;

            call = RESTClient.getLocationHstoryData(getActivity(), body, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    if (dialog != null)
                        dialog.dismiss();

                    showToastFromBgThread("Error in getting history location data,Please refresh.");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {

                            String res = response.body().string();

                            System.out.println("LocationData:" + res);
                            JSONObject jsonObject = new JSONObject(res);
                            if (jsonObject.has("valid")) {
                                String valid = jsonObject.getString("valid");
                                if (valid.equalsIgnoreCase("true")) {

                                    JSONArray jsonArray = jsonObject.getJSONArray("object");
                                    if (jsonArray.length() > 0) {

                                        for (int y = 0; y < jsonArray.length(); y++) {
                                            JSONObject jsonObject1 = jsonArray.getJSONObject(y);
                                            JSONArray jsonArray_trip = jsonObject1.getJSONArray("trips");

                                            if (jsonArray_trip.length() > 0) {

                                                for (int j = 0; j < jsonArray_trip.length(); j++) {
                                                    JSONArray jsonArray1 = jsonArray_trip.getJSONArray(j);
                                                    if (jsonArray1 != null && jsonArray1.length() > 0) {

                                                        ArrayList<LocationPoints> route = new ArrayList<LocationPoints>();
                                                        for (int i = 0; i < jsonArray1.length(); i++) {
                                                            JSONObject jsonObject2 = jsonArray1.getJSONObject(i);

                                                            double lat = 0;
                                                            double lng = 0;
                                                            if (jsonObject2.has("latitude")) {
                                                                lat = Double.parseDouble(jsonObject2.getString("latitude"));
                                                            }
                                                            if (jsonObject2.has("longitude")) {
                                                                lng = Double.parseDouble(jsonObject2.getString("longitude"));
                                                            }
                                                            LatLng source_l = new LatLng(lat, lng);
                                                            LocationPoints locationPoints = new LocationPoints();
                                                            locationPoints.latLng = source_l;

                                                            if (jsonObject2.has("address")) {
                                                                locationPoints.address = jsonObject2.getString("address");
                                                            }

                                                            if (jsonObject2.has("sys_timestamp")) {
                                                                locationPoints.logTime = jsonObject2.getString("sys_timestamp");
                                                            }
                                                            if (jsonObject2.has("distance")) {
                                                                locationPoints.distance = jsonObject2.getString("distance");
                                                            }
                                                            route.add(locationPoints);


                                                            polylineOptions.add(source_l);
                                                            polylineOptions2.add(source_l);
                                                        }

                                                        routeList.add(route);

                                                        PolylineOptions[] PolylineOptions_array = new PolylineOptions[2];
                                                        PolylineOptions_array[0] = polylineOptions;
                                                        PolylineOptions_array[1] = polylineOptions2;
                                                        polylineOptionsesList.add(PolylineOptions_array);
                                                    }
                                                }

                                            }
                                        }
                                    }
                                } else {
                                    showToastFromBgThread("Error in getting history location data,Please refresh.");
                                }
                            } else {
                                showToastFromBgThread("Error in getting history location data,Please refresh.");
                            }
                        } catch (Exception e) {
                            showToastFromBgThread("Error in data parsing.Please try again.");
                        } finally {
                            try {
                                dialog.dismiss();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            if (map != null) {
                                                map.clear();
                                                /*if (polylineOptionsesList != null && polylineOptionsesList.size() > 0) {

                                                    for (int i = 0; i < polylineOptionsesList.size(); i++) {
                                                        map.addPolyline(polylineOptionsesList.get(i)[0]);
                                                        map.addPolyline(polylineOptionsesList.get(i)[1]);
                                                    }
                                                }*/
                                                setLocationOnMap();

                                                Collections.reverse(routeList);
                                                routePolyline.clear();
                                                for (int i = 0; i < routeList.size(); i++) {
                                                    ArrayList<LocationPoints> latLngs = routeList.get(i);
                                                    callRouteDesignerDirect(latLngs);
                                                }
                                                initTripList();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                ((Flint) getActivity().getApplication()).trackException(e);
                                showToastFromBgThread("Error in plotting history location data,Please refresh.");
                            }

                        }
                    } else {
                        dialog.dismiss();
                        showToastFromBgThread("Error in getting history location data,Please refresh.");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }
    }

    private void showToastFromBgThread(final String message) {

        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        if (map != null)
                            map.clear();
                        setLocationOnMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openPicker(String tag, String current) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(formatWithoutTime.parse(current));
            DatePickerDialog.newInstance(this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                    .show(getActivity().getFragmentManager(), tag);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String pattern = dayOfMonth + " " + GeneralUtilities.getMonth(monthOfYear) + " " + year;
        try {
            switch (view.getTag()) {
                case prefixFrom:
                    to = formatWithoutTime.parse(txtToDate.getText().toString());
                    from = formatWithoutTime.parse(pattern);
                    txtFromDate.setText(pattern);
                    break;
                case prefixTo:
                    to = formatWithoutTime.parse(pattern);
                    from = formatWithoutTime.parse(txtFromDate.getText().toString());
                    txtToDate.setText(pattern);
                    break;
            }
            to.setHours(23);
            to.setMinutes(59);
            to.setSeconds(59);
            if (from.before(to)) {

                long difference = to.getTime() - from.getTime();
                long diffHours = difference / (60 * 60 * 1000);
                /*if (diffHours > 24 * 2) {
                    Toast.makeText(getActivity(), "Invalid date range.Keep difference of one day only.", Toast.LENGTH_SHORT).show();
                } else {
                    loadHistoryTrackingData(formatWithTime.format(from), formatWithTime.format(to));
                }*/
                loadHistoryTrackingData(formatWithTime.format(from), formatWithTime.format(to));

            } else {
                Toast.makeText(getActivity(), "Invalid Dates! Please review dates again.", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void callRouteDesignerDirect(ArrayList<LocationPoints> route) {

        try {
            if (route.size() > 1) {
                RouteDesigner designer = new RouteDesigner()
                        .setMap(map)
                        .setContext(getActivity())
                        .setOrigin(route.get(0).latLng)
                        .setDestination(route.get(route.size() - 1).latLng)
                        .setBaseLayer(new PolylineOptions().width(10).color(Color.parseColor("#546E7A")).geodesic(true))
                        .setUpperLayer(new PolylineOptions().width(5).color(Color.parseColor("#90A4AE")).geodesic(true))
                        .setResponseListener(new RouteDesigner.DesignerListener() {
                            @Override
                            public void onRequestCompleted(String json, final Polyline[] polylines) {
                                routePolyline.add(polylines);
                            }

                            @Override
                            public void onRequestFailure(Exception e) {
                            }
                        });

                if (route.size() > 2) {
                    //Route has more than two points
                    LatLng[] latLngs = new LatLng[route.size() - 2];
                    for (int i = 1; i < route.size() - 1; i++) {
                        latLngs[i - 1] = route.get(i).latLng;
                    }
                    designer.design(latLngs);
                } else {
                    //Route has only two points ,Source and Destination,No mid points are there
                    designer.design();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void initTripList() {


        ListView listView = (ListView) convertView.findViewById(R.id.list);
        //Set Trip Related Values in Header
        if (routeList.size() > 0) {
            try {

                donutProgressengine_load_indicator.setProgress(GeneralUtilities.generateRandomNumber(65, 85));
                ((TextView) convertView.findViewById(R.id.txt_trips_count)).setText(routeList.size() + "");

                //Calculating total distance in all Trips between selected date range
                float total_distance = 0;
                for (int i = 0; i < routeList.size(); i++) {
                    try {
                        total_distance = total_distance + Float.parseFloat(routeList.get(i).get(0).distance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ((TextView) convertView.findViewById(R.id.txt_trips_total_km)).setText(String.format("%.2f", total_distance) + "");
                ((TextView) convertView.findViewById(R.id.txt_trip_kmpl)).setText("14.3");

                /*Calculate Total Duration of All Trips in between given Date range*/
                long total_trip_travel_time_in_millis = 0;
                for (int i = 0; i < routeList.size(); i++) {
                    ArrayList<LocationPoints> locationPointses = routeList.get(i);
                    String fromDate_String = locationPointses.get(0).logTime;
                    String toDate_String = locationPointses.get(locationPointses.size() - 1).logTime;


                    System.out.println("fromDate_String :"+fromDate_String+" toDate_String:"+toDate_String);

                    long fromDate_in_millis = GeneralUtilities.convertDateStringToMilliseconds(fromDate_String, "yyyy-MM-dd HH:mm:ss");
                    long toDate_in_millis = GeneralUtilities.convertDateStringToMilliseconds(toDate_String, "yyyy-MM-dd HH:mm:ss");

                    /*long fromDate_in_millis = Long.parseLong(fromDate_String);
                    long toDate_in_millis = Long.parseLong(toDate_String);*/

                    System.out.println("fromDate_String long :"+fromDate_in_millis+" toDate_String:"+toDate_in_millis);

                    if (toDate_in_millis >= fromDate_in_millis) {
                        total_trip_travel_time_in_millis = total_trip_travel_time_in_millis + (toDate_in_millis - fromDate_in_millis);
                    }
                }
                try {
                    int minutes = (int) ((total_trip_travel_time_in_millis / (1000 * 60)) % 60);
                    int hours = (int) ((total_trip_travel_time_in_millis / (1000 * 60 * 60)) % 24);


                    if (minutes < 10)
                        ((TextView) convertView.findViewById(R.id.txt_trip_hours)).setText(hours + ":0" + minutes);
                    else
                        ((TextView) convertView.findViewById(R.id.txt_trip_hours)).setText(hours + ":" + minutes);

                }catch (Exception e)
                {
                    e.printStackTrace();
                }


                convertView.findViewById(R.id.view_empty_content).setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            donutProgressengine_load_indicator.setProgress(0);
            ((TextView) convertView.findViewById(R.id.txt_trips_count)).setText("0");
            ((TextView) convertView.findViewById(R.id.txt_trips_total_km)).setText("0");
            ((TextView) convertView.findViewById(R.id.txt_trip_kmpl)).setText("0");
            ((TextView) convertView.findViewById(R.id.txt_trip_hours)).setText("0");

            convertView.findViewById(R.id.view_empty_content).setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
        tripAddapter = new TripAddapter();
        listView.setAdapter(tripAddapter);
        findAdderesStringOfLocation();
    }

    private void setSourceDestinationMarker(LatLng source, LatLng destination) {
        try {
            if (sourceMarker != null && dstinationMarker != null) {
                sourceMarker.setPosition(source);
                dstinationMarker.setPosition(destination);
            } else {
                MarkerOptions source_marker = new MarkerOptions().position(source).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_a));
                MarkerOptions destination_marker = new MarkerOptions().position(destination).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_b));
                // adding marker
                sourceMarker = map.addMarker(source_marker);
                dstinationMarker = map.addMarker(destination_marker);
            }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(source, 13));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int total_distance_covered = 0;

    private void getDistanceOfRoutes(ArrayList<ArrayList<LocationPoints>> routesList) {

        eachTripDistance = new String[routesList.size()];

        String origins = null;
        String destinations = null;

        for (int i = 0; i < routesList.size(); i++) {
            ArrayList<LocationPoints> locationPointses = routesList.get(i);

            if (origins == null)
                origins = locationPointses.get(0).latLng.latitude + "," + locationPointses.get(0).latLng.longitude;
            else
                origins = origins + "|" + locationPointses.get(0).latLng.latitude + "," + locationPointses.get(0).latLng.longitude;

            if (destinations == null)
                destinations = locationPointses.get(locationPointses.size() - 1).latLng.latitude + "," + locationPointses.get(locationPointses.size() - 1).latLng.longitude;
            else
                destinations = destinations + "|" + locationPointses.get(locationPointses.size() - 1).latLng.latitude + "," + locationPointses.get(locationPointses.size() - 1).latLng.longitude;
        }

        final String CLIENT_ID = "gme-teramatrixtechnologies";
        final String CRYPTO_KEY = "XO8V3tNa30yrEDOtQ4NjN2WoOQg=";


        DistanceCalculator calculator = new DistanceCalculator()
                .setOrigins(origins)
                .setMode(TravelMode.MODE_DRIVING)
                .setServerKey("AIzaSyA7JfopI53LYvrcU-tfoAT1trWGNM6UQOU")
                .setResponseListener(new DistanceCalculator.DistanceListener() {
                    @Override
                    public void onRequestCompleted(String json, ArrayList<Distance> distances) {

                        try {
                            for (int i = 0; i < distances.size(); i++) {
                                Distance distance = distances.get(i);
                                int k = (int) distance.getDistanceValue() / 1000;
                                int y = ((int) distance.getDistanceValue() % 1000) / 100;
                                eachTripDistance[i] = k + "." + y;
                                total_distance_covered = total_distance_covered + (int) distance.getDistanceValue();


                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) convertView.findViewById(R.id.txt_trips_total_km)).setText(total_distance_covered / 1000 + "");

                            }
                        });

                    }

                    @Override
                    public void onRequestFailure(Exception e) {
                        Log.e("DIRECT", e.getMessage());
                    }
                });
        calculator.execute(destinations);

        initTripList();

    }


    private void findAdderesStringOfLocation() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (dialog != null)
                    dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    for (int i = 0; i < routeList.size(); i++) {
                        final ArrayList<LocationPoints> locationPointses = routeList.get(i);
                        LatLng source = locationPointses.get(0).latLng;
                        LatLng destinatino = locationPointses.get(locationPointses.size() - 1).latLng;

                        String source_address = getCityNameFromLocation(source.latitude, source.longitude);
                        String destiantion_addresss = getCityNameFromLocation(destinatino.latitude, destinatino.longitude);
                        locationPointses.get(0).address = source_address;
                        locationPointses.get(locationPointses.size() - 1).address = destiantion_addresss;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                if (tripAddapter != null)
                    tripAddapter.notifyDataSetChanged();

                try {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(routeList.get(0).get(0).latLng, 12));
                } catch (Exception e) {
                    try {
                        double lat = Double.parseDouble(HomeFragment.obdData.get("latitude").get(HomeFragment.PARAM_VALUE));
                        double lng = Double.parseDouble(HomeFragment.obdData.get("longitude").get(HomeFragment.PARAM_VALUE));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12));
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private String getCityNameFromLocation(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String cityName = addresses.get(0).getAddressLine(0);
            String stateName = addresses.get(0).getAddressLine(1);
            String countryName = addresses.get(0).getAddressLine(2);
            return cityName + "," + stateName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        try {
            if (new GeneralUtilities(getActivity()).isConnected()) {
                loadHistoryTrackingData(txtFromDate.getText().toString() + " 00:00:00", txtToDate.getText().toString() + " 23:59:59");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }

    }

    class TripAddapter extends BaseAdapter {
        private int selectedPosition = -1;
        ViewHolder lastSelectedViewHolder;
        Polyline[] highlighted_Polylines;

        @Override
        public int getCount() {
            return routeList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.trip_summery_layout, null);
                viewHolder.txt_a = (TextView) convertView.findViewById(R.id.txt_a);
                viewHolder.txt_b = (TextView) convertView.findViewById(R.id.txt_b);
                viewHolder.txt_from_date = (TextView) convertView.findViewById(R.id.txt_from_date);
                viewHolder.txt_to_date = (TextView) convertView.findViewById(R.id.txt_to_date);
                viewHolder.txt_from_place = (TextView) convertView.findViewById(R.id.txt_from_place);
                viewHolder.txt_to_place = (TextView) convertView.findViewById(R.id.txt_to_place);
                viewHolder.txt_duration = (TextView) convertView.findViewById(R.id.txt_duration);
                viewHolder.graph_view = convertView.findViewById(R.id.graph_view);
                viewHolder.txt_trip_distance = (TextView) convertView.findViewById(R.id.txt_trip_distance);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final ArrayList<LocationPoints> locationPointses = routeList.get(position);


            viewHolder.txt_from_place.setText(locationPointses.get(0).address);
            viewHolder.txt_to_place.setText(locationPointses.get(locationPointses.size() - 1).address);

            //set formated From Date
            String fromDate = locationPointses.get(0).logTime;
            String fromDate_milliseconds = GeneralUtilities.convertDateStringToMilliseconds(fromDate,"yyyy-MM-dd HH:mm:ss")+"";
            fromDate = GeneralUtilities.format_DateString_From_Millisecond_To_Another_Pattern(fromDate_milliseconds, "hh:mm aaa");
            viewHolder.txt_from_date.setText(fromDate);

            //set formated To Date
            String toDate = locationPointses.get(locationPointses.size() - 1).logTime;
            String toDate_milliseconds = GeneralUtilities.convertDateStringToMilliseconds(toDate,"yyyy-MM-dd HH:mm:ss")+"";
            toDate = GeneralUtilities.format_DateString_From_Millisecond_To_Another_Pattern(toDate_milliseconds, "hh:mm aaa") + "\n" + GeneralUtilities.format_DateString_From_Millisecond_To_Another_Pattern(toDate_milliseconds, "MMM dd");
            viewHolder.txt_to_date.setText(toDate);

            String duration = GeneralUtilities.getTimeOffset(fromDate_milliseconds,toDate_milliseconds);
            viewHolder.txt_duration.setText(duration);

            viewHolder.txt_trip_distance.setText(locationPointses.get(0).distance);

            if (selectedPosition == position) {
                viewHolder.txt_a.setBackgroundResource(R.drawable.round_corner_selected);
                viewHolder.txt_b.setBackgroundResource(R.drawable.round_corner_selected);
            } else {
                viewHolder.txt_a.setBackgroundResource(R.drawable.round_corner);
                viewHolder.txt_b.setBackgroundResource(R.drawable.round_corner);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastSelectedViewHolder != null) {
                        lastSelectedViewHolder.txt_a.setBackgroundResource(R.drawable.round_corner);
                        lastSelectedViewHolder.txt_b.setBackgroundResource(R.drawable.round_corner);
                    }
                    viewHolder.txt_a.setBackgroundResource(R.drawable.round_corner_selected);
                    viewHolder.txt_b.setBackgroundResource(R.drawable.round_corner_selected);
                    lastSelectedViewHolder = viewHolder;
                    selectedPosition = position;
                    panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                    //Highlight Selected trip path on map through different color
                    try {

                        //Set Default path color to previously selected path
                        if (highlighted_Polylines != null) {
                            highlighted_Polylines[0].setColor(Color.parseColor("#546E7A"));
                            highlighted_Polylines[1].setColor(Color.parseColor("#90A4AE"));
                        }

                        //Highlight selected path
                        Polyline[] polylines = routePolyline.get(position);
                        polylines[0].setColor(Color.parseColor("#00BCD4"));
                        polylines[1].setColor(Color.parseColor("#00BCD4"));


                        //Set Marker on Source and Destination
                        setSourceDestinationMarker(locationPointses.get(0).latLng, locationPointses.get(locationPointses.size() - 1).latLng);

                        highlighted_Polylines = polylines;


                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });

            viewHolder.graph_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Start Analytic Screen
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView txt_a;
            TextView txt_b;
            TextView txt_to_date;
            TextView txt_from_date;
            TextView txt_to_place, txt_from_place;
            TextView txt_duration;
            TextView txt_trip_distance;

            View graph_view;

        }
    }
}
