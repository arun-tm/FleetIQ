package com.teramatrix.fleetiq.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.teramatrix.fleetiq.Interface.IRefreshFragment;
import com.teramatrix.fleetiq.MainActivity;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.velocimeter.velocimeterlibrary.VelocimeterView;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class VehicleHealthFragment extends Fragment implements IRefreshFragment{


    private View convertView;
    private SpeedometerGauge speedometer;
    private VelocimeterView velocimeter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.vehicle_health_fragment, null, false);
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
            GeneralUtilities.setUpActionBar(getActivity(), this, "Vehicle Health Monitoring", null);

        /*Animation animation =AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_anim);
        animation.setFillAfter(true);*/

            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(5000);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.setRepeatCount(-1);


        /*convertView.findViewById(R.id.icon1).startAnimation(rotate);
        convertView.findViewById(R.id.icon2).startAnimation(rotate);
        convertView.findViewById(R.id.icon3).startAnimation(rotate);
        convertView.findViewById(R.id.icon4).startAnimation(rotate);*/


        /*Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.truck_chasis_2);
        bm = highlightImage(bm);



        ImageView imageView = (ImageView)convertView.findViewById(R.id.img_radial_menu);
        imageView.setImageBitmap(bm);*/

            ((TextView) convertView.findViewById(R.id.txt_total_ignitions)).setText(HomeFragment.obdData.get("total_ignition_no").get(HomeFragment.PARAM_VALUE).toString() + "");
            ((TextView) convertView.findViewById(R.id.txt_error_code_counts)).setText(HomeFragment.obdData.get("current_Error_code_numbers").get(HomeFragment.PARAM_VALUE).toString() + "");
            ((TextView) convertView.findViewById(R.id.txt_total_harsh_accelaration)).setText(HomeFragment.obdData.get("total_harsh_acceleration_no").get(HomeFragment.PARAM_VALUE).toString() + "");
            ((TextView) convertView.findViewById(R.id.txt_total_hash_breaks_value)).setText(HomeFragment.obdData.get("total_harsh").get(HomeFragment.PARAM_VALUE).toString() + "");

            /*txt_engine_load*/
            ((TextView) convertView.findViewById(R.id.txt_engine_load)).setText(HomeFragment.obdData.get("engine_load").get(HomeFragment.PARAM_VALUE).toString() + " %");
            setParametersProgressBarValues(convertView.findViewById(R.id.bar_engine_load),HomeFragment.obdData.get("engine_load").get(HomeFragment.PARAM_VALUE).toString(),HomeFragment.obdData.get("engine_load").get(HomeFragment.PARAM_MIN).toString(),HomeFragment.obdData.get("engine_load").get(HomeFragment.PARAM_MAX).toString());

            /*bar_coolent_temp*/
            ((TextView) convertView.findViewById(R.id.txt_coolent_temp)).setText(HomeFragment.obdData.get("coolant_temperature").get(HomeFragment.PARAM_VALUE).toString() + " " + (char) 0x00B0 + "C");
            setParametersProgressBarValues(convertView.findViewById(R.id.bar_coolen_temp), HomeFragment.obdData.get("coolant_temperature").get(HomeFragment.PARAM_VALUE).toString(), HomeFragment.obdData.get("coolant_temperature").get(HomeFragment.PARAM_MIN).toString(), HomeFragment.obdData.get("coolant_temperature").get(HomeFragment.PARAM_MAX).toString());


            /*bar_throttel_opening*/
            ((TextView) convertView.findViewById(R.id.txt_throttel_opening)).setText(HomeFragment.obdData.get("throttle_opening_width").get(HomeFragment.PARAM_VALUE).toString() + " %");
            setParametersProgressBarValues(convertView.findViewById(R.id.bar_throttel_opening), HomeFragment.obdData.get("throttle_opening_width").get(HomeFragment.PARAM_VALUE).toString(), HomeFragment.obdData.get("throttle_opening_width").get(HomeFragment.PARAM_MIN).toString(), HomeFragment.obdData.get("throttle_opening_width").get(HomeFragment.PARAM_MAX).toString());

            /*bar_fuel_consumption*/
            ((TextView) convertView.findViewById(R.id.txt_fuel_consumption)).setText(HomeFragment.obdData.get("total_fuel_consumption_volume").get(HomeFragment.PARAM_VALUE).toString() + " L");
            setParametersProgressBarValues(convertView.findViewById(R.id.bar_fuel_consumption), HomeFragment.obdData.get("total_fuel_consumption_volume").get(HomeFragment.PARAM_VALUE).toString(), HomeFragment.obdData.get("total_fuel_consumption_volume").get(HomeFragment.PARAM_MIN).toString(), HomeFragment.obdData.get("total_fuel_consumption_volume").get(HomeFragment.PARAM_MAX).toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setParametersProgressBarValues(View bar,String current_value,String min_value,String max_value)
    {
        try {

            if(current_value == null || min_value== null || max_value== null)
                return;
            if(current_value.equalsIgnoreCase("null") || min_value.equalsIgnoreCase("null") || max_value.equalsIgnoreCase("null"))
                return;

            float currentValue = Float.parseFloat(current_value);
            float minValue = Float.parseFloat(min_value);
            float maxValue = Float.parseFloat(max_value);

            float percent = (((currentValue - minValue) * 100) / (maxValue - minValue));

            LinearLayout.LayoutParams bar_param = (LinearLayout.LayoutParams) (bar.getLayoutParams());
            bar_param.weight = percent;
            bar.setLayoutParams(bar_param);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void refreshFragment() {
        initViews();
    }
}
