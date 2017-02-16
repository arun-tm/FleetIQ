package com.teramatrix.fleetiq.fragments;

import android.graphics.Color;
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
import android.widget.ArrayAdapter;

import com.rey.material.widget.Spinner;
import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.adapter.AlertAdapter;
import com.teramatrix.fleetiq.custom.MyBarChart;
import com.teramatrix.fleetiq.model.AlertModel;

import java.util.ArrayList;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class CustomChartFragment extends Fragment {


    private View convertView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.custom_fragment, null, false);

        /*convertView.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyBarChart)convertView.findViewById(R.id.custom_bar_chart)).refreshView();
            }
        });*/

        try {
            MyBarChart myBarChart = (MyBarChart) convertView.findViewById(R.id.custom_bar_chart);


//            myBarChart.setViewBackgroundColor(Color.parseColor("#112731"));
            myBarChart.setViewBackgroundColor(Color.parseColor("#263238"));
            myBarChart.setGraphLineStyle(Color.parseColor("#00BCD4"), 4);
            myBarChart.setHorizontalDimentionValues(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
//            myBarChart.setHorizontalDimentionValues(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"});
            myBarChart.setVerticalDimentionValues(new int[11]);
            myBarChart.setWallpainted(true);
            myBarChart.setVerticleBackgorundLine(true, Color.GRAY, 4);
            myBarChart.setTextStyle(Color.GRAY,23,25);

            myBarChart.refreshView();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

            Spinner spn_no_arrow =  (Spinner)convertView.findViewById(R.id.spinner_vehicle);
            String[] items = new String[]{"All Vehicle","Honda city","Scross","Scorpio"};

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_spn_dropdown,items);
            adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
            spn_no_arrow.setAdapter(adapter);
            spn_no_arrow.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(Spinner parent, View view, int position, long id) {
                    if(position == 0)
                    {
                        //Show All Vehicle

                    }else
                    {
                        //show Single Vehicle

                    }

                }
            });



            Spinner spinner_driven =  (Spinner)convertView.findViewById(R.id.spinner_driven);
            String[] items_spinner_driven = new String[]{"This Week","This month","This year"};

            ArrayAdapter<String> adapter_spinner_driven = new ArrayAdapter<>(getActivity(), R.layout.row_spn_dropdown,items_spinner_driven);
            adapter_spinner_driven.setDropDownViewResource(R.layout.row_spn_dropdown);
            spinner_driven.setAdapter(adapter_spinner_driven);


            Spinner spinner_graph =  (Spinner)convertView.findViewById(R.id.spinner_graph);
            String[] items_spinner_graph = new String[]{"Distance","Fuel Average","Duration"};

            ArrayAdapter<String> adapter_spinner_graph = new ArrayAdapter<>(getActivity(), R.layout.row_spn_dropdown,items_spinner_graph);
            adapter_spinner_graph.setDropDownViewResource(R.layout.row_spn_dropdown);
            spinner_graph.setAdapter(adapter_spinner_graph);

        } catch (Exception e) {
            e.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(e);
        }
    }

}
