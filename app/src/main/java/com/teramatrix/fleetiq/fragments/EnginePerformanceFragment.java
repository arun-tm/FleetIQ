package com.teramatrix.fleetiq.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.Interface.IRefreshFragment;
import com.teramatrix.fleetiq.MainActivity;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.Util;
import com.teramatrix.fleetiq.adapter.ComponentAttributeListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class EnginePerformanceFragment extends Fragment implements IRefreshFragment {


    private View convertView;
    private DonutProgress donutProgressengine_load_indicator;
    private DonutProgress donutProgressengine_rpm_indicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_engine_performance, null, false);

        }

        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
        ((MainActivity) getActivity()).setOpenedFragment(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        try {

            menu.findItem(R.id.action_refresh).setVisible(true);
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
                if (new GeneralUtilities(getActivity()).isConnected()) {
                    getActivity().sendBroadcast(new Intent("ACTION_DASHBOARD_DATA_UPDATE"));
                } else {
                    Toast.makeText(getActivity(), "Netwok not available!!", Toast.LENGTH_SHORT).show();
                }

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {

        try {

            GeneralUtilities.setUpActionBar(getActivity(), this, "Engine Performance", null);

            //Setup Engine Load Progress indicator
            String engine_load_min_value = HomeFragment.obdData.get("engine_load").get(HomeFragment.PARAM_MIN).toString();
            String engine_load_max_value = HomeFragment.obdData.get("engine_load").get(HomeFragment.PARAM_MAX).toString();
            float engine_load_current_value = Float.parseFloat(HomeFragment.obdData.get("engine_load").get(HomeFragment.PARAM_VALUE).toString());

            donutProgressengine_load_indicator = (DonutProgress) convertView.findViewById(R.id.engine_load_indicator);
            donutProgressengine_load_indicator.setMax(100);
            donutProgressengine_load_indicator.setProgress(0);
            donutProgressengine_load_indicator.setTextSize(65);
            donutProgressengine_load_indicator.setUnfinishedStrokeColor(getActivity().getResources().getColor(R.color.blueGray_dark));

            Util.animateDonut(getActivity(), donutProgressengine_load_indicator, (int) Math.ceil(engine_load_current_value));

            if ((int) Math.ceil(engine_load_current_value) == 0) {
                //default state
                donutProgressengine_load_indicator.setTextColor(Color.WHITE);

            } else if ((int) Math.ceil(engine_load_current_value) < 75) {
                //Its Ok
                donutProgressengine_load_indicator.setFinishedStrokeColor(getActivity().getResources().getColor(R.color.donut_default_color));
                donutProgressengine_load_indicator.setTextColor(getActivity().getResources().getColor(R.color.donut_default_color));
            } else {
                //alert situation
                donutProgressengine_load_indicator.setFinishedStrokeColor(getActivity().getResources().getColor(R.color.rpm_meter_color_outside));
                donutProgressengine_load_indicator.setTextColor(getActivity().getResources().getColor(R.color.rpm_meter_color_outside));
                Animation anim = new AlphaAnimation(0.4f, 1.0f);
                anim.setDuration(350); //You can manage the blinking time with this parameter
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                donutProgressengine_load_indicator.startAnimation(anim);
            }


            //Setup Engine Rpm Progress indicator

            String rpm_min_value = HomeFragment.obdData.get("engine_speed").get(HomeFragment.PARAM_MIN).toString();
            String rpm_max_value = HomeFragment.obdData.get("engine_speed").get(HomeFragment.PARAM_MAX).toString();
            int rpm_current_value = Integer.parseInt(HomeFragment.obdData.get("engine_speed").get(HomeFragment.PARAM_VALUE).toString());

            donutProgressengine_rpm_indicator = (DonutProgress) convertView.findViewById(R.id.egine_rpm);
            donutProgressengine_rpm_indicator.setMax(Integer.parseInt(rpm_max_value));
            donutProgressengine_rpm_indicator.setProgress(0);
            donutProgressengine_rpm_indicator.setTextSize(65);
            donutProgressengine_rpm_indicator.setTextColor(Color.WHITE);
            donutProgressengine_rpm_indicator.setSuffixText("");
            donutProgressengine_rpm_indicator.setUnfinishedStrokeColor(getActivity().getResources().getColor(R.color.blueGray_dark));

            Util.animateDonut(getActivity(), donutProgressengine_rpm_indicator, rpm_current_value);


            initAttributeList();
            //set other engine related field values
            /*String engine_coolant_temperature = HomeFragment.obdData.get("coolant_temperature").get(HomeFragment.PARAM_VALUE).toString();
            ((TextView) convertView.findViewById(R.id.engine_coolant_temperature)).setText(HomeFragment.obdData.get("coolant_temperature").get(HomeFragment.PARAM_VALUE).toString() + " C");*/

        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }

    }

    private void initAttributeList() {
        try {
            ListView listView = (ListView) convertView.findViewById(R.id.listView);

            ArrayList<HashMap<String, String>> hashMaps = new ArrayList<HashMap<String, String>>();

            Set<String> keys = HomeFragment.obdData.keySet();
            for (String key : keys) {
                try {
                    String service_name = HomeFragment.obdData.get(key).get(HomeFragment.PARAM_SERVICE_NAME);
                    if (service_name.equalsIgnoreCase("throttle_opening_width")) {
                        try {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put(HomeFragment.PARAM_ALIAS, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_ALIAS));
                            hashMap.put(HomeFragment.PARAM_VALUE, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_VALUE));
                            hashMap.put(HomeFragment.PARAM_UNIT, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_UNIT));
                            hashMaps.add(hashMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    String service_name = HomeFragment.obdData.get(key).get(HomeFragment.PARAM_SERVICE_NAME);
                    if (service_name.equalsIgnoreCase("temperature")) {
                        try {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put(HomeFragment.PARAM_ALIAS, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_ALIAS));
                            hashMap.put(HomeFragment.PARAM_VALUE, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_VALUE));
                            hashMap.put(HomeFragment.PARAM_UNIT, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_UNIT));
                            hashMaps.add(hashMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    String service_name = HomeFragment.obdData.get(key).get(HomeFragment.PARAM_SERVICE_NAME);
                    if (service_name.equalsIgnoreCase("current_Error_code_numbers") || service_name.equalsIgnoreCase("harsh_acceleration_no") || service_name.equalsIgnoreCase("harsh_brake_no")) {
                        try {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put(HomeFragment.PARAM_ALIAS, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_ALIAS));
                            hashMap.put(HomeFragment.PARAM_VALUE, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_VALUE));
                            hashMap.put(HomeFragment.PARAM_UNIT, HomeFragment.obdData.get(key).get(HomeFragment.PARAM_UNIT));
                            hashMaps.add(hashMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (hashMaps.size() == 0) {
                convertView.findViewById(R.id.txt_no_data_found).setVisibility(View.VISIBLE);
            } else {
                convertView.findViewById(R.id.txt_no_data_found).setVisibility(View.GONE);
            }
            listView.setAdapter(new ComponentAttributeListAdapter(getActivity(), hashMaps));
        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

    @Override
    public void refreshFragment() {
        initViews();
    }
}
