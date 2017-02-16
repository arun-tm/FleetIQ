package com.teramatrix.fleetiq.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
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
public class FuelAnalyticFragment extends Fragment implements IRefreshFragment {


    private View convertView;
    private DonutProgress donutProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_fuel_analytic, null, false);
            initViews();
        }

        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();
        GeneralUtilities.setUpActionBar(getActivity(), this, "Fuel Performance", null);
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
                if(new GeneralUtilities(getActivity()).isConnected())
                {
                    getActivity().sendBroadcast(new Intent("ACTION_DASHBOARD_DATA_UPDATE"));
                }else
                {
                    Toast.makeText(getActivity(), "Netwok not available!!", Toast.LENGTH_SHORT).show();
                }

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {

        try {


            //Setup Fuel Progress indicator
            String fuel_min_value = HomeFragment.obdData.get("total_fuel_consumption_volume").get(HomeFragment.PARAM_MIN).toString();
            String fuel_max_value = HomeFragment.obdData.get("total_fuel_consumption_volume").get(HomeFragment.PARAM_MAX).toString();
            float fuel_current_value = Float.parseFloat(HomeFragment.obdData.get("total_fuel_consumption_volume").get(HomeFragment.PARAM_VALUE).toString());

            int fuel_per = (int) (fuel_current_value * 100 / Float.parseFloat(fuel_max_value));


            convertView.findViewById(R.id.fuel_progress_indicator).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CarScoreFragment carScoreFragment = new CarScoreFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("screen", "Fuel Score");
                    carScoreFragment.setArguments(bundle);
                    GeneralUtilities.loadFragment((AppCompatActivity) getActivity(), carScoreFragment);
                }
            });
            donutProgress = (DonutProgress) convertView.findViewById(R.id.fuel_progress_indicator);
            /*donutProgress.setMax(Integer.parseInt(fuel_max_value));*/
            donutProgress.setMax(10);
            donutProgress.setProgress(0);
            donutProgress.setTextSize(65);
            donutProgress.setSuffixText("");
            /*if(fuel_per>=20)
            {
                donutProgress.setFinishedStrokeColor(getActivity().getResources().getColor(R.color.rpm_meter_color_outside));
                donutProgress.setTextColor(getActivity().getResources().getColor(R.color.rpm_meter_color_outside));
                Animation anim = new AlphaAnimation(0.4f, 1.0f);
                anim.setDuration(350); //You can manage the blinking time with this parameter
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
//                donutProgress.startAnimation(anim);
            }else
            {
                donutProgress.setFinishedStrokeColor(getActivity().getResources().getColor(R.color.fuel_meter_color_outside));
                donutProgress.setTextColor(getActivity().getResources().getColor(R.color.fuel_meter_color_outside));
            }*/

            donutProgress.setFinishedStrokeColor(getActivity().getResources().getColor(R.color.fuel_meter_color_outside));
            donutProgress.setUnfinishedStrokeColor(getActivity().getResources().getColor(R.color.blueGray_dark));
            donutProgress.setTextColor(getActivity().getResources().getColor(R.color.fuel_meter_color_outside));

//            Util.animateDonut(getActivity(),donutProgress,fuel_per);


            //Animate Fuel Score
            try {
                String txt_score = HomeFragment.obdData.get("score").get(HomeFragment.PARAM_VALUE);
                if (txt_score != null && txt_score.length() > 0) {
                    float score = Float.parseFloat(txt_score);
                    if (score == 0)
                        Util.animateDonut(getActivity(), donutProgress, 0);
                    else {
                        int fuel_score = GeneralUtilities.generateRandomNumber(6, 8);
                        Util.animateDonut(getActivity(), donutProgress, 6);
                    }
                } else {
                    Util.animateDonut(getActivity(), donutProgress, 0);
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }


            //set fuel range
            String driving_range_value = HomeFragment.obdData.get("driving_range").get(HomeFragment.PARAM_VALUE).toString();
            ((TextView) convertView.findViewById(R.id.txt_can_go)).setText(driving_range_value);

            //set fuel average
            try {
                String fuel_avg = HomeFragment.obdData.get("average_fuel_consumption").get(HomeFragment.PARAM_VALUE).toString();
                float fueAvg = 100 / Float.parseFloat(fuel_avg);
                fuel_avg = String.format("%.02f", fueAvg);
                ((TextView) convertView.findViewById(R.id.txt_mpg)).setText(fuel_avg);
            } catch (Exception e) {
                e.printStackTrace();
            }


            //Load other attribute List
            initAttributeList();

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
                    if (service_name.equalsIgnoreCase("fuel")) {
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
