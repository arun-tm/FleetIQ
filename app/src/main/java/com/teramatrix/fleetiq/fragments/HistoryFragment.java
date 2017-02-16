package com.teramatrix.fleetiq.fragments;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.SPUtils;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class HistoryFragment extends Fragment {


    private View convertView;

    private String[] month = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.layout_history, null, false);
        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        try {
            menu.findItem(R.id.action_graph).setVisible(false);
            menu.findItem(R.id.action_refresh).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    private void initViews()
    {

        LinearLayout linearLayout = (LinearLayout)convertView.findViewById(R.id.hor_scroll_container);

        Resources r = getResources();


        for(int i =0;i<12;i++)
        {
            View  v =LayoutInflater.from(getActivity()).inflate(R.layout.history_row,null);

            int randomNum = new Random().nextInt((110 - 20) + 1) + 20;
            ((TextView)v.findViewById(R.id.txt_value)).setText(randomNum + "");
            ((TextView)v.findViewById(R.id.txt_month)).setText(month[i]);
            LinearLayout view = (LinearLayout)v.findViewById(R.id.bar_month);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();

            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, randomNum, r.getDisplayMetrics());

            layoutParams.height = (int)px;
            view.setLayoutParams(layoutParams);

            linearLayout.addView(v);
        }
    }
}
