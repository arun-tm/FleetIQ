package com.teramatrix.fleetiq.fragments;

import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.rey.material.widget.RadioButton;
import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.Interface.IRefreshFragment;
import com.teramatrix.fleetiq.MainActivity;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.SPUtils;
import com.teramatrix.fleetiq.Utils.Util;
import com.teramatrix.fleetiq.controller.RESTClient;
import com.teramatrix.fleetiq.model.OBDVehicelInfo;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by arun.singh on 6/6/2016.
 */
public class HomeFragment extends Fragment implements GoogleMap.OnMapLoadedCallback, IRefreshFragment, OnMapReadyCallback {

    private View convertView;
    private ImageView img_radial_menu;
    private GoogleMap map;
    double latitude = 26.917;
    double longitude = 75.817;
    private AlertDialog dialog;
    private Call call;


    public static String PARAM_VALUE = "param_value";
    public static String PARAM_UNIT = "param_unit";
    public static String PARAM_MAX = "param_max";
    public static String PARAM_MIN = "param_min";
    public static String PARAM_ALIAS = "param_alias";
    public static String PARAM_SERVICE_NAME = "service_name";
    public static String PARAM_SYSTEM_TIMESTAMP = "sys_timestamp";

    private String API_ID = "";
    private String API_GET_LOCATION_DATA = "location_data";
    private String API_GET_OBD_DATA = "obd_data";

    private OBDVehicelInfo obdVehicelInfo;

    public static HashMap<String, HashMap<String, String>> obdData = new HashMap<String, HashMap<String, String>>();
    public static ArrayList<OBDVehicelInfo> obdVehicelInfoArrayList = new ArrayList<OBDVehicelInfo>();

    private DonutProgress donutProgressengine_load_indicator;

    private boolean is_network_error_message_shown;

    BroadcastReceiver dataRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                callAPI(API_GET_OBD_DATA);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    BroadcastReceiver newObdDeviceSelection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String selected_bd_device_id = intent.getStringExtra("selected_bd_device_id");
            configueAppFromSelectedODBDevice(selected_bd_device_id);
        }
    };

    BroadcastReceiver getAllConnectedObdDeviceInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Call Api to get all data from OBD Device
            getDeviceInfo();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        obdData.clear();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.home_fragment, null, false);

                initializeMap(savedInstanceState);
                initViews();
                getActivity().registerReceiver(dataRefreshReceiver, new IntentFilter("ACTION_DASHBOARD_DATA_UPDATE"));
                getActivity().registerReceiver(newObdDeviceSelection, new IntentFilter("ACTION_OBD_SOURCE_CHANGED"));
                getActivity().registerReceiver(getAllConnectedObdDeviceInfo, new IntentFilter("ACTION_GET_ALL_OBD_SOURCE"));


                if (GeneralUtilities.isConnected(getActivity()))
                    getDeviceInfo();

            }
            return convertView;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String methodName = "printMyName";
    private String valueObject = "Flint";

    @Override
    public void onResume() {

        try {
            super.onResume();
            GeneralUtilities.setUpActionBar(getActivity(), this, "FLINT", null);
            ((MainActivity) getActivity()).setOpenedFragment(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {


            super.onDestroy();
            getActivity().unregisterReceiver(dataRefreshReceiver);
            getActivity().unregisterReceiver(newObdDeviceSelection);
            getActivity().unregisterReceiver(getAllConnectedObdDeviceInfo);
            if (call != null) call.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        try {
            //Disable map selection from bottom View
            convertView.findViewById(R.id.map_belo_parent).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            //Disable map selection from Score View
            convertView.findViewById(R.id.car_score_indicator).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        CarScoreFragment carScoreFragment = new CarScoreFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("screen", "Vehicle Score");
                        carScoreFragment.setArguments(bundle);

                        RadialViewFragment radialViewFragment = new RadialViewFragment();

                        GeneralUtilities.loadFragment((AppCompatActivity) getActivity(), carScoreFragment);
                    }
                    return true;
                }
            });

            img_radial_menu = (ImageView) convertView.findViewById(R.id.img_radial_menu);

            convertView.findViewById(R.id.txt_scan).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    img_radial_menu.setBackgroundResource(R.drawable.hover1);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                    img_radial_menu.setBackgroundResource(R.drawable.normal);

                        ScanFragment scanFragment = new ScanFragment();
                        GeneralUtilities.loadFragment((AppCompatActivity) getActivity(), scanFragment);
                    }

                    return true;
                }
            });
            convertView.findViewById(R.id.txt_track).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    img_radial_menu.setBackgroundResource(R.drawable.hover2);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                    img_radial_menu.setBackgroundResource(R.drawable.normal);

                        MapFragment mapFragment = new MapFragment();
                        GeneralUtilities.loadFragment((AppCompatActivity) getActivity(), mapFragment);
                    }

                    return true;
                }
            });
            convertView.findViewById(R.id.txt_dashboard).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    img_radial_menu.setBackgroundResource(R.drawable.hover3);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                    img_radial_menu.setBackgroundResource(R.drawable.normal);


                        DashboardFragment dashboardFragment = new DashboardFragment();
                        GeneralUtilities.loadFragment((AppCompatActivity) getActivity(), dashboardFragment);

                    }

                    return true;
                }
            });
            convertView.findViewById(R.id.txt_alert).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    img_radial_menu.setBackgroundResource(R.drawable.hover4);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                    img_radial_menu.setBackgroundResource(R.drawable.normal);

                        /*AlertFragment alertFragment = new AlertFragment();
                        GeneralUtilities.loadFragment((AppCompatActivity) getActivity(), alertFragment);*/
                    }

                    return true;
                }
            });


            //Car Score
            donutProgressengine_load_indicator = (DonutProgress) convertView.findViewById(R.id.car_score_indicator);
            donutProgressengine_load_indicator.setMax(10);
            donutProgressengine_load_indicator.setProgress(0);
            donutProgressengine_load_indicator.setTextSize(60);
            donutProgressengine_load_indicator.setFinishedStrokeColor(getActivity().getResources().getColor(R.color.external_progress));
            donutProgressengine_load_indicator.setTextColor(Color.WHITE);
            donutProgressengine_load_indicator.setSuffixText("");
            donutProgressengine_load_indicator.setFinishedStrokeWidth(20);
            donutProgressengine_load_indicator.setUnfinishedStrokeWidth(20);
            Util.animateDonut(getActivity(), donutProgressengine_load_indicator, 0);


            dialog = new SpotsDialog(getActivity(), R.style.Custom);


            GeneralUtilities.getDatePeriodRange("week", null, 0);

        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void initViewsWithRealValues() {

        try {

            //Set Car Score
            try {
                String txt_score = obdData.get("score").get(PARAM_VALUE);
                float score = Float.parseFloat(txt_score);
                donutProgressengine_load_indicator.setProgress(0);
                Util.animateDonut(getActivity(), donutProgressengine_load_indicator, Math.round(score));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Set Location On Map
            try {
                String latitude = obdData.get("latitude").get(PARAM_VALUE);
                String longitude = obdData.get("longitude").get(PARAM_VALUE);
                this.latitude = Double.parseDouble(latitude);
                this.longitude = Double.parseDouble(longitude);
                setUserPositionOnMap();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Set Values in Views
            try {
                ((TextView) convertView.findViewById(R.id.txt_model_name)).setText(new SPUtils(getActivity()).getVehicleModel() + "");
                ((TextView) convertView.findViewById(R.id.txt_reg_number)).setText(new SPUtils(getActivity()).getVehicleRegistrationNumber() + "");
                ((TextView) convertView.findViewById(R.id.txt_trip_value)).setText(obdData.get("total_driving_time").get(PARAM_VALUE).toString() + " Hrs");
                ((TextView) convertView.findViewById(R.id.txt_drivername)).setText(new SPUtils(getActivity()).getString(SPUtils.VEHICLE_DRIVER_NAME) + "");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Set Account Title/SubTitle in Navigation Drawer Header
            ((MainActivity) getActivity()).setAccountInfo(" " + new SPUtils(getActivity()).getVehicleModel(), " " + new SPUtils(getActivity()).getVehicleRegistrationNumber() + "");

        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        try {
            menu.findItem(R.id.action_graph).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {

                try {
                /*Refresh Ticket Details. Call All Ticket APIs Again*/
                    if (GeneralUtilities.isConnected(getActivity())) {

                        callAPI(API_GET_OBD_DATA);
                    } else {
                        Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    callAPI(API_GET_OBD_DATA);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        snackbar.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Flint) getActivity().getApplication()).trackException(e);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapLoaded() {
//        setUserPositionOnMap();
    }

    private void setUserPositionOnMap() {
        try {
            if (new GeneralUtilities(getActivity()).isConnected()) {
                // create marker
                MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_vehical_marker));
                // adding marker
                map.addMarker(marker);

                CameraPosition cameraPosition = new CameraPosition.Builder().
                        target(new LatLng(latitude, longitude)).
                        tilt(80).
                        zoom(17).
                        bearing(0).
                        build();

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(, 16));
            /*CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude)).zoom(2).build();*/
            }
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void initializeMap(Bundle savedInstanceState) {

        try {
            //Obtaining our MapView defined in xml
            final MapView mapView = (MapView) convertView.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            MapsInitializer.initialize(getActivity());
//            map = mapView.getMap();
            mapView.getMapAsync(this);
            /*if (map != null)
                map.setOnMapLoadedCallback(this);*/


        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }

    }

    private void callAPI(final String api_id) throws Exception {
        dialog.show();
        SPUtils spUtils = new SPUtils(getActivity());
        String body = "token=" + spUtils.getToken() +
                "&deviceid=" + spUtils.getDeviceID() +
                "&access_key=" + spUtils.getAccessKey() +
                "&userkey=" + spUtils.getUserKey() +
                "&user_id=" + spUtils.getUserID();


        if (api_id.equalsIgnoreCase(API_GET_OBD_DATA)) {

            call = RESTClient.getDeviceData(getActivity(), body, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    try {
                        dialog.dismiss();
                        Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    callAPI(API_GET_OBD_DATA);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        snackbar.show();
                    } catch (Exception ee) {
                        e.printStackTrace();
                        ((Flint) getActivity().getApplication()).trackException(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {

                            String res = response.body().string();
                            System.out.println("API_GET_OBD_DATA: "+res);
                            JSONObject jsonObject = new JSONObject(res);
                            if (jsonObject.has("valid")) {
                                String valid = jsonObject.getString("valid");
                                if (valid.equalsIgnoreCase("true")) {

                                    JSONArray jsonArray = jsonObject.getJSONArray("object");

                                    if (jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                            String param_key = jsonObject1.getString("data_source");

                                            System.out.println("param_key " + param_key);

                                            HashMap<String, String> hashMap = new HashMap<String, String>();

                                            if (jsonObject1.has("current_value"))
                                                hashMap.put(PARAM_VALUE, jsonObject1.getString("current_value"));
                                            if (jsonObject1.has("service_servicedatasource_unit"))
                                                hashMap.put(PARAM_UNIT, jsonObject1.getString("service_servicedatasource_unit"));
                                            if (jsonObject1.has("service_servicedatasource_max_value"))
                                                hashMap.put(PARAM_MAX, jsonObject1.getString("service_servicedatasource_max_value"));
                                            if (jsonObject1.has("service_servicedatasource_min_value"))
                                                hashMap.put(PARAM_MIN, jsonObject1.getString("service_servicedatasource_min_value"));
                                            if (jsonObject1.has("service_servicedatasource_alias"))
                                                hashMap.put(PARAM_ALIAS, jsonObject1.getString("service_servicedatasource_alias"));
                                            if (jsonObject1.has("service_name"))
                                                hashMap.put(PARAM_SERVICE_NAME, jsonObject1.getString("service_name"));
                                            if (jsonObject1.has("sys_timestamp"))
                                                hashMap.put(PARAM_SYSTEM_TIMESTAMP, jsonObject1.getString("sys_timestamp"));


                                            obdData.put(param_key, hashMap);
                                        }
                                    } else {

                                        nullifyAllObdParamValues();
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "This Device has no data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                }

                            }
                        } catch (Exception e) {
                            try {
                                e.printStackTrace();
                                Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                                snackbar.setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            callAPI(API_GET_OBD_DATA);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                snackbar.show();
                            } catch (Exception ee) {
                                e.printStackTrace();
                                ((Flint) getActivity().getApplication()).trackException(e);
                            }
                        } finally {
                            dialog.dismiss();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callAPI(API_GET_LOCATION_DATA);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });


                        }
                    } else {
                        try {
                            dialog.dismiss();
                            Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        callAPI(API_GET_OBD_DATA);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            snackbar.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            ((Flint) getActivity().getApplication()).trackException(e);
                        }
                    }
                }
            });
        } else if (api_id.equalsIgnoreCase(API_GET_LOCATION_DATA)) {
           /*----------------------------------------------------------------------------------------------------------------------------*/
            // Get Location Data from OBD Device
            call = RESTClient.getLocationData(getActivity(), body, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    try {


                        dialog.dismiss();
                        Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    callAPI(API_GET_OBD_DATA);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        snackbar.show();
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {

                            String res = response.body().string();
                            JSONObject jsonObject = new JSONObject(res);
                            if (jsonObject.has("valid")) {
                                String valid = jsonObject.getString("valid");
                                if (valid.equalsIgnoreCase("true")) {

                                    JSONArray jsonArray = jsonObject.getJSONArray("object");

                                    if (jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                            String param_key = jsonObject1.getString("data_source");

                                            HashMap<String, String> hashMap = new HashMap<String, String>();

                                            if (jsonObject1.has("current_value"))
                                                hashMap.put(PARAM_VALUE, jsonObject1.getString("current_value"));
                                            if (jsonObject1.has("service_servicedatasource_unit"))
                                                hashMap.put(PARAM_UNIT, jsonObject1.getString("service_servicedatasource_unit"));
                                            if (jsonObject1.has("service_servicedatasource_max_value"))
                                                hashMap.put(PARAM_MAX, jsonObject1.getString("service_servicedatasource_max_value"));
                                            if (jsonObject1.has("service_servicedatasource_min_value"))
                                                hashMap.put(PARAM_MIN, jsonObject1.getString("service_servicedatasource_min_value"));
                                            if (jsonObject1.has("service_servicedatasource_alias"))
                                                hashMap.put(PARAM_ALIAS, jsonObject1.getString("service_servicedatasource_alias"));
                                            if (jsonObject1.has("service_name"))
                                                hashMap.put(PARAM_SERVICE_NAME, jsonObject1.getString("service_name"));
                                            if (jsonObject1.has("sys_timestamp"))
                                                hashMap.put(PARAM_SYSTEM_TIMESTAMP, jsonObject1.getString("sys_timestamp"));

                                            obdData.put(param_key, hashMap);
                                        }
                                    }

                                }

                            }
                        } catch (Exception e) {
                            try {
                                e.printStackTrace();
                                Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                                snackbar.setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            callAPI(API_GET_OBD_DATA);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                                snackbar.show();
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        } finally {
                            dialog.dismiss();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {

                                        //Save Trip's start Latitude/longitude to draw a trail on map
                                        if (obdData.get("engine_status").get(PARAM_VALUE).equalsIgnoreCase("0")) {
                                            //Engine is off, Vehicle is stopped
                                            new SPUtils(getActivity()).setValue(SPUtils.TRIP_START_LATITUDE, "");
                                            new SPUtils(getActivity()).setValue(SPUtils.TRIP_START_LONGITUDE, "");

                                        } else if (obdData.get("engine_status").get(PARAM_VALUE).equalsIgnoreCase("1")) {
                                            //Engine is on
                                            String startlat = new SPUtils(getActivity()).getString(SPUtils.TRIP_START_LATITUDE);
                                            if (startlat == null || startlat.equalsIgnoreCase("")) {
                                                //Last saved trip start lat lng are empty. Put Value into them as trip has stated now
                                                new SPUtils(getActivity()).setValue(SPUtils.TRIP_START_LATITUDE, obdData.get("latitude").get(PARAM_VALUE).toString());
                                                new SPUtils(getActivity()).setValue(SPUtils.TRIP_START_LONGITUDE, obdData.get("longitude").get(PARAM_VALUE).toString());
                                            }
                                        }

                                        //Filter all OBD data values for showing
                                        filterOBDDataTagAndValues();


                                        //Refresh View with latest obd data
                                        if (((IRefreshFragment) ((MainActivity) getActivity()).getOpenedFragment()) != null)
                                            ((IRefreshFragment) ((MainActivity) getActivity()).getOpenedFragment()).refreshFragment();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ((Flint) getActivity().getApplication()).trackException(e);
                                    }
                                }
                            });

                        }
                    } else {
                        try {
                            dialog.dismiss();
                            Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        callAPI(API_GET_OBD_DATA);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            snackbar.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            ((Flint) getActivity().getApplication()).trackException(e);
                        }
                    }
                }
            });
        }
    }


    private void initUserListDialog(final ArrayList<OBDVehicelInfo> vehicelInfoArrayList) {

        try {
            final android.support.v7.app.AlertDialog dialog;
            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            alertDialog.setTitle("Select Vehicle");
            dialog = alertDialog.create();
            dialog.getWindow().setDimAmount(0.7f);

            View hostVIew = LayoutInflater.from(getActivity()).inflate(R.layout.layout_tanker_listview, null);
            dialog.setView(hostVIew);
            final ListView listView = (ListView) hostVIew.findViewById(R.id.listview_tankers);

            listView.setAdapter(new BaseAdapter() {

                View selectedRowView;
                ViewHolder selectedViewHolder;

                @Override
                public int getCount() {
                    return vehicelInfoArrayList.size();
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
                        convertView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_tankers_list, null);
                        viewHolder.textView = (TextView) convertView.findViewById(R.id.tv_tanker);
                        viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radio_btn);
                        viewHolder.selectedRowView = convertView;
                        convertView.setTag(viewHolder);

                    } else {
                        viewHolder = (ViewHolder) convertView.getTag();
                    }


                    viewHolder.textView.setText(vehicelInfoArrayList.get(position).model_code);


                    if (obdVehicelInfo != null && obdVehicelInfo.registration_no.equalsIgnoreCase(vehicelInfoArrayList.get(position).registration_no))
                        viewHolder.radioButton.setChecked(true);
                    else
                        viewHolder.radioButton.setChecked(false);

                    viewHolder.selectedRowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (selectedViewHolder != null) {
                                //uncheck previously selected radiobutton
                                selectedViewHolder.selectedRowView.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
                                selectedViewHolder.radioButton.setChecked(false);
                            }
                            //check currently selected radiobutton
                            selectedViewHolder = viewHolder;
                            selectedViewHolder.selectedRowView.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
                            viewHolder.radioButton.setChecked(true);

                            obdVehicelInfo = vehicelInfoArrayList.get(position);
                        }
                    });
                    viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (selectedViewHolder != null) {
                                //uncheck previously selected radiobutton
                                selectedViewHolder.selectedRowView.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
                                selectedViewHolder.radioButton.setChecked(false);
                            }
                            //check currently selected radiobutton
                            selectedViewHolder = viewHolder;
                            selectedViewHolder.selectedRowView.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
                            viewHolder.radioButton.setChecked(true);

                            obdVehicelInfo = vehicelInfoArrayList.get(position);
                        }
                    });

                    return convertView;
                }

                class ViewHolder {
                    View selectedRowView;
                    TextView textView;
                    RadioButton radioButton;
                }

            });
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (obdVehicelInfo == null) {
                        Toast.makeText(getActivity(), "Select a Vehicle", Toast.LENGTH_SHORT).show();
                    } else {
                        configueAppFromSelectedODBDevice(obdVehicelInfo.device_id);
                        dialog.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void getDeviceInfo() {
        dialog.show();

        String body = "token=" + new SPUtils(getActivity()).getToken() +
                "&userkey=" + new SPUtils(getActivity()).getUserKey() +
                "&user_id=" + new SPUtils(getActivity()).getUserID();


        call = RESTClient.DeviceInfo(getActivity(), body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dialog.dismiss();
                Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getDeviceInfo();
                    }
                });
                snackbar.show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final ArrayList<OBDVehicelInfo> obdVehicelInfos = new ArrayList<OBDVehicelInfo>();
                    try {

                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        if (jsonObject.has("valid")) {
                            String valid = jsonObject.getString("valid");
                            if (valid.equalsIgnoreCase("true")) {

                                JSONArray jsonObject1 = jsonObject.getJSONArray("object");
                                if (jsonObject1.length() > 0) {
                                    for (int i = 0; i < jsonObject1.length(); i++) {
                                        OBDVehicelInfo obdVehicelInfo = new OBDVehicelInfo();
                                        JSONObject jsonObject2 = jsonObject1.getJSONObject(i);
                                        if (jsonObject2.has("creation_time"))
                                            obdVehicelInfo.chasis_number = jsonObject2.getString("creation_time");
                                        if (jsonObject2.has("address"))
                                            obdVehicelInfo.address = jsonObject2.getString("address");
                                        if (jsonObject2.has("gender"))
                                            obdVehicelInfo.gender = jsonObject2.getString("gender");
                                        if (jsonObject2.has("device_id"))
                                            obdVehicelInfo.device_id = jsonObject2.getString("device_id");
                                        if (jsonObject2.has("user_name"))
                                            obdVehicelInfo.user_name = jsonObject2.getString("user_name");
                                        if (jsonObject2.has("is_owner"))
                                            obdVehicelInfo.is_owner = jsonObject2.getString("is_owner");
                                        if (jsonObject2.has("vehicle_type"))
                                            obdVehicelInfo.vehicle_type = jsonObject2.getString("vehicle_type");
                                        if (jsonObject2.has("user_key"))
                                            obdVehicelInfo.user_key = jsonObject2.getString("user_key");
                                        if (jsonObject2.has("registration_no"))
                                            obdVehicelInfo.registration_no = jsonObject2.getString("registration_no");
                                        if (jsonObject2.has("condition"))
                                            obdVehicelInfo.condition = jsonObject2.getString("condition");
                                        if (jsonObject2.has("device_name"))
                                            obdVehicelInfo.device_name = jsonObject2.getString("device_name");
                                        if (jsonObject2.has("model_code"))
                                            obdVehicelInfo.model_code = jsonObject2.getString("model_code");
                                        if (jsonObject2.has("vin"))
                                            obdVehicelInfo.vin = jsonObject2.getString("vin");
                                        if (jsonObject2.has("chasis_number"))
                                            obdVehicelInfo.chasis_number = jsonObject2.getString("chasis_number");
                                        if (jsonObject2.has("vehicle_status"))
                                            obdVehicelInfo.vehicle_status = jsonObject2.getString("vehicle_status");
                                        if (jsonObject2.has("fuel_type"))
                                            obdVehicelInfo.fuel_type = jsonObject2.getString("fuel_type");
                                        if (jsonObject2.has("purchase_date"))
                                            obdVehicelInfo.purchase_date = jsonObject2.getString("purchase_date");
                                        if (jsonObject2.has("org_name"))
                                            obdVehicelInfo.org_name = jsonObject2.getString("org_name");
                                        if (jsonObject2.has("vehicle_id"))
                                            obdVehicelInfo.vehicle_id = jsonObject2.getString("vehicle_id");
                                        if (jsonObject2.has("color_code"))
                                            obdVehicelInfo.color_code = jsonObject2.getString("color_code");
                                        if (jsonObject2.has("is_exists"))
                                            obdVehicelInfo.is_exists = jsonObject2.getString("is_exists");
                                        if (jsonObject2.has("driver_name"))
                                            obdVehicelInfo.driver_name = jsonObject2.getString("driver_name");

                                        obdVehicelInfos.add(obdVehicelInfo);
                                    }
                                }
                                /*startActivity(new Intent(Login.this, MainActivity.class));
                                finish();*/
                            }
                        } else {
                            try {


                                Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.login_unsuccess), Snackbar.LENGTH_LONG);
                                snackbar.setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getDeviceInfo();
                                    }
                                });
                                snackbar.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        try {
                            e.printStackTrace();
                            Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getDeviceInfo();
                                }
                            });
                            snackbar.show();
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                    } finally {
                        dialog.dismiss();


                        obdVehicelInfoArrayList.clear();
                        obdVehicelInfoArrayList.addAll(obdVehicelInfos);


                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    if (new SPUtils(getActivity()).getDeviceID() != null && new SPUtils(getActivity()).getDeviceID().length() > 0) {
                                        //User has already selected a vehicle .
                                        configueAppFromSelectedODBDevice(new SPUtils(getActivity()).getDeviceID());
                                    } else {
                                        //User has not selected a vehicle yet, show him a selection dialog list.
                                        initUserListDialog(obdVehicelInfos);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ((Flint) getActivity().getApplication()).trackException(e);
                                }
                            }
                        });

                    }
                } else {
                    try {
                        dialog.dismiss();
                        Snackbar snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                        snackbar.setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                            login();
                                getDeviceInfo();
                            }
                        });
                        snackbar.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((Flint) getActivity().getApplication()).trackException(e);
                    }
                }
            }
        });
    }


    private void saveVehicleModelDetails(OBDVehicelInfo obdVehicelInfo) {
        SPUtils spUtils = new SPUtils(getActivity());
        spUtils.setValue(SPUtils.DEVICE_ID, obdVehicelInfo.device_id);
        spUtils.setValue(SPUtils.VEHICLE_MODEL, obdVehicelInfo.model_code);
        spUtils.setValue(SPUtils.VEHICLE_REGISTRATION_NUMBER, obdVehicelInfo.registration_no);
        spUtils.setValue(SPUtils.VEHICLE_ADDRESS, obdVehicelInfo.address);
        spUtils.setValue(SPUtils.VEHICLE_USERNAME, obdVehicelInfo.user_name);
        spUtils.setValue(SPUtils.VEHICLE_TYPE, obdVehicelInfo.vehicle_type);
        spUtils.setValue(SPUtils.VEHICLE_CONDITION, obdVehicelInfo.condition);
        spUtils.setValue(SPUtils.VEHICLE_ENGINE_NUMBER, obdVehicelInfo.engine_number);
        spUtils.setValue(SPUtils.VEHICLE_CHASIS_NUMBER, obdVehicelInfo.chasis_number);
        spUtils.setValue(SPUtils.VEHICLE_STATUS, obdVehicelInfo.vehicle_status);
        spUtils.setValue(SPUtils.VEHICLE_FUEL_TYPE, obdVehicelInfo.fuel_type);
        spUtils.setValue(SPUtils.VEHICLE_PURCHASE_DATE, obdVehicelInfo.purchase_date);
        spUtils.setValue(SPUtils.VEHICLE_COLOR_CODE, obdVehicelInfo.color_code);
        spUtils.setValue(SPUtils.VEHICLE_DRIVER_NAME, obdVehicelInfo.driver_name);
        spUtils.setValue(SPUtils.VEHICLE_OBD_DEVICE_NAME, obdVehicelInfo.device_name);
    }

    //Setup Account(ODB) list in Navigation Drawer.
    //Setup details in views(Complete App) for selected odb device
    private void configueAppFromSelectedODBDevice(String selected_odb_device_id) {
        try {
            OBDVehicelInfo selected_obdVehicelInfo = null;
            ArrayList<OBDVehicelInfo> obdVehicelInfos_nav_drawer_list = new ArrayList<OBDVehicelInfo>();
            for (OBDVehicelInfo obdVehicelInfo : obdVehicelInfoArrayList) {
                if (obdVehicelInfo.device_id.equalsIgnoreCase(selected_odb_device_id)) {
                    selected_obdVehicelInfo = obdVehicelInfo;
                } else {
                    obdVehicelInfos_nav_drawer_list.add(obdVehicelInfo);
                }
            }

            //Save Selected ODB Device Details in SP
            saveVehicleModelDetails(selected_obdVehicelInfo);
            //Initialize Selected Account(ODB Device) details in navigation Drawer
            ((MainActivity) getActivity()).setAccountInfo(selected_obdVehicelInfo.model_code, selected_obdVehicelInfo.registration_no);
            //Initialize Account(ODB Device) list in navigation Drawer
            ((MainActivity) getActivity()).setAccountsInNavigationDrawer(obdVehicelInfos_nav_drawer_list);

            new SPUtils(getActivity()).setValue(SPUtils.DEVICE_ID, selected_obdVehicelInfo.device_id);

            //call obd data api
            callAPI(API_GET_OBD_DATA);

        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    private void filterOBDDataTagAndValues() {
        if (obdData.get("engine_status").get(PARAM_VALUE).toString().equalsIgnoreCase("0")) {
//            nullifyAllObdParamValues();
            //set value-tag to engine_status parameter
            obdData.get("engine_status").put(PARAM_VALUE, "OFF");
            obdData.get("engine_status").put(PARAM_UNIT, "");
        } else {
            //set value-tag to engine_status parameter
            obdData.get("engine_status").put(PARAM_VALUE, "ON");
            obdData.get("engine_status").put(PARAM_UNIT, "");
        }
    }

    private void nullifyAllObdParamValues() {
        Set<String> keys = obdData.keySet();
        for (String key : keys) {

            obdData.get(key).put(PARAM_MIN, "0");
            obdData.get(key).put(PARAM_MAX, "0");
            obdData.get(key).put(PARAM_VALUE, "0");
        }
    }

    @Override
    public void refreshFragment() {
//        callAPI(API_GET_OBD_DATA);
        initViewsWithRealValues();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        System.out.println("MAP1 Loaded");
    }
}
