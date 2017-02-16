package com.teramatrix.fleetiq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.SPUtils;
import com.teramatrix.fleetiq.fragments.HomeFragment;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_setting);

            setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_arrow_back_white_24dp);
            initViews();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews()
    {
        try {
            String location_last_updated_time = HomeFragment.obdData.get("latitude").get(HomeFragment.PARAM_SYSTEM_TIMESTAMP);
            String obd_data_last_updated_time = HomeFragment.obdData.get("running_speed").get(HomeFragment.PARAM_SYSTEM_TIMESTAMP);
            String vehicle_obd_device_name = new SPUtils(this).getString(SPUtils.VEHICLE_OBD_DEVICE_NAME);



            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(0);

            location_last_updated_time = df.format(Double.parseDouble(location_last_updated_time))+"";
            obd_data_last_updated_time = df.format(Double.parseDouble(obd_data_last_updated_time))+"";

            location_last_updated_time = GeneralUtilities.format_DateString_From_Millisecond_To_Another_Pattern(location_last_updated_time, "dd MMM yyyy hh:mm:ss aaaa");
            obd_data_last_updated_time = GeneralUtilities.format_DateString_From_Millisecond_To_Another_Pattern(obd_data_last_updated_time, "dd MMM yyyy hh:mm:ss aaaa");


            ((TextView) findViewById(R.id.txt_location_update)).setText(location_last_updated_time);
            ((TextView) findViewById(R.id.txt_data_update)).setText(obd_data_last_updated_time);
            ((TextView) findViewById(R.id.txt_device_idn)).setText(vehicle_obd_device_name);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
