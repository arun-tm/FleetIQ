package com.teramatrix.fleetiq.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.rey.material.widget.Spinner;
import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.custom.MyBarChart;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class RadialViewFragment extends Fragment {


    private View convertView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.radial_view_layout, null, false);

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

            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.action_graph).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        try {
            GeneralUtilities.setUpActionBar(getActivity(), this, "Trip Aggregation", null);

        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

}
