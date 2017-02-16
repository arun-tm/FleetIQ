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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.Interface.IRefreshFragment;
import com.teramatrix.fleetiq.MainActivity;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class AllParametersFragment extends Fragment implements IRefreshFragment {
    private View convertView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_all_parameters, null, false);
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
        }catch (Exception e)
        {
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
            GeneralUtilities.setUpActionBar(getActivity(), this, "All Parameters", null);
            Set<String> allKeys =  HomeFragment.obdData.keySet();

            //Sort Hash map on Keys value
            SortedSet<String> keys = new TreeSet<String>(allKeys);

            final ArrayList<String> stringsKeys = new ArrayList<String>();
            for(String key:keys)
            {
                System.out.println(" waha---" + key + "   " + HomeFragment.obdData.get(key).get(HomeFragment.PARAM_VALUE));

                if(key.equalsIgnoreCase("latitude") || key.equalsIgnoreCase("longitude"))
                    continue;
                else
                stringsKeys.add(key);
            }
            ListView listView =  (ListView)convertView.findViewById(R.id.listView);
            listView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return stringsKeys.size();
                }
                @Override
                public Object getItem(int position) {
                    return stringsKeys.get(position);

                }
                @Override
                public long getItemId(int position) {
                    return position;
                }
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    ViewHolder viewHolder = null;
                    if(convertView == null)
                    {
                        viewHolder = new ViewHolder();
                        convertView =  LayoutInflater.from(getActivity()).inflate(R.layout.component_attribure_row_2,null);

                        viewHolder.paramName = ((TextView)convertView.findViewById(R.id.attribute_name));
                        viewHolder.paramValue = ((TextView)convertView.findViewById(R.id.txt_value));
                        viewHolder.paramUnit = ((TextView)convertView.findViewById(R.id.txt_unit));

                        convertView.setTag(viewHolder);
                    }else
                    {
                        viewHolder =  (ViewHolder)convertView.getTag();
                    }

                    viewHolder.paramName.setText(HomeFragment.obdData.get(stringsKeys.get(position)).get(HomeFragment.PARAM_ALIAS));
                    viewHolder.paramValue.setText(GeneralUtilities.getRoundOffValue(HomeFragment.obdData.get(stringsKeys.get(position)).get(HomeFragment.PARAM_VALUE),2));
                    viewHolder.paramUnit.setText(HomeFragment.obdData.get(stringsKeys.get(position)).get(HomeFragment.PARAM_UNIT));

                    return convertView;
                }
                class ViewHolder
                {
                    TextView paramName;
                    TextView paramValue;
                    TextView paramUnit;
                }
            });
            /*LinearLayout linearLayoutContainer = (LinearLayout)convertView.findViewById(R.id.container);


            for(String key :allKeys)
            {
                if(key.equalsIgnoreCase("latitude") || key.equalsIgnoreCase("longitude"))
                    continue;
                View row =  LayoutInflater.from(getActivity()).inflate(R.layout.component_attribure_row_2,null);
                String key_alias = HomeFragment.obdData.get(key).get(HomeFragment.PARAM_ALIAS);
                String key_value = HomeFragment.obdData.get(key).get(HomeFragment.PARAM_VALUE);
                String key_value_unit = HomeFragment.obdData.get(key).get(HomeFragment.PARAM_UNIT);

                ((TextView)row.findViewById(R.id.attribute_name)).setText(key_alias);
                ((TextView)row.findViewById(R.id.txt_value)).setText(key_value);
                ((TextView)row.findViewById(R.id.txt_unit)).setText(key_value_unit);
                linearLayoutContainer.addView(row);

            }*/
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
