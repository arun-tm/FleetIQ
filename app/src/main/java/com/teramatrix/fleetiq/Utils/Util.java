package com.teramatrix.fleetiq.Utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.fragments.FuelAnalyticFragment;
import com.teramatrix.fleetiq.fragments.VehicleHealthFragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class Util {

    public static void setUpActionBar(Activity activity, Fragment fragment, String title, String subtitle) {

        AppCompatActivity a = (AppCompatActivity) activity;
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        a.setTitle(title);
        if (subtitle != null) {
            a.getSupportActionBar().setSubtitle(subtitle);
        }else
            a.getSupportActionBar().setSubtitle(null);

        if (fragment instanceof FuelAnalyticFragment) {
            a.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
            a.getSupportActionBar().setHomeButtonEnabled(true);
            a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else if(fragment instanceof VehicleHealthFragment)
        {
            a.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
            a.getSupportActionBar().setHomeButtonEnabled(true);
            a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else {
            /*a.getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_arrow_back_white_24dp);*/
        }
    }
    public static void report(final Activity activity, final int v, final int msg) {
        final AppCompatActivity a = (AppCompatActivity) activity;
        if (a != null)
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(a.findViewById(v), a.getResources().getString(msg), Snackbar.LENGTH_LONG).show();
                }
            });
    }



    public static void animateDonut(final Activity context,final DonutProgress donutProgress,final int value)
    {
        int repeat_time = 1000;
        if(value>100)
            repeat_time = 30;
        else if(value>10)
            repeat_time = 120;
        else
            repeat_time = 250;

        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int i =0;
            @Override
            public void run() {
                if(i>=value) {
                    timer.cancel();
                }
                else {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (value > 500) {
                                donutProgress.setProgress(i);
                                i = i + 20;
                            } else {
                                donutProgress.setProgress(i);
                                i = i + 1;
                            }
                            if (i >= value)
                                donutProgress.setProgress(value);
                        }
                    });

                }
            }
        };
        timer.schedule(timerTask,0,repeat_time);
    }

}
