package com.teramatrix.fleetiq.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.adapter.AlertAdapter;
import com.teramatrix.fleetiq.model.AlertModel;

import java.util.ArrayList;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class AlertFragment extends Fragment {


    private View convertView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.alert_fragment,null,false);
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
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initViews()
    {
        try {
            GeneralUtilities.setUpActionBar(getActivity(), this, "Alert", null);

            RecyclerView recyclerView = (RecyclerView) convertView.findViewById(R.id.recycler_view);

            ArrayList<AlertModel> alertModelArrayList = new ArrayList<AlertModel>();
            alertModelArrayList.add(new AlertModel("A1025", "Your fuel level is less then 30%"));
            alertModelArrayList.add(new AlertModel("P0123", "Evaporative Emission (EVAP) System Small Leak Detected"));
            alertModelArrayList.add(new AlertModel("P0420", "Catalyst System Low Efficiency"));
            alertModelArrayList.add(new AlertModel("A1030", "Your rear left tyre has low air pressure"));

//        mAdapter = new MoviesAdapter(movieList);
            AlertAdapter alertAdapter = new AlertAdapter(alertModelArrayList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(alertAdapter);

        }catch (Exception e)
        {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }
}
