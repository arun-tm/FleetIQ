package com.teramatrix.fleetiq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arun.singh on 6/25/2016.
 */
public class ComponentAttributeListAdapter extends BaseAdapter {

    private ArrayList<HashMap<String,String>> hashMapArrayList;
    private Context context;
    public ComponentAttributeListAdapter(Context context,ArrayList<HashMap<String,String>> hashMapArrayList)
    {
        this.hashMapArrayList = hashMapArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return hashMapArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return hashMapArrayList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.component_attribure_row, null, false);
            viewHolder.viewAlertBar = convertView.findViewById(R.id.indicator);
            viewHolder.attributeName = (TextView)convertView.findViewById(R.id.attribute_name);
            viewHolder.attributeValue = (TextView)convertView.findViewById(R.id.attribute_value);
            convertView.setTag(viewHolder);
        }else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        HashMap<String,String> hashMap =  hashMapArrayList.get(position);
        String isAlert =  hashMap.get("isAlert");
        String name =  hashMap.get(HomeFragment.PARAM_ALIAS);
        String value =  hashMap.get(HomeFragment.PARAM_VALUE) +" "+hashMap.get(HomeFragment.PARAM_UNIT);

        viewHolder.attributeName.setText(name);
        viewHolder.attributeValue.setText(value);

        return convertView;
    }
    class  ViewHolder
    {
        View viewAlertBar;
        TextView attributeName;
        TextView attributeValue;
    }
}
