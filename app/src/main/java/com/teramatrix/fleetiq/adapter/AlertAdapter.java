package com.teramatrix.fleetiq.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.model.AlertModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arun.singh on 6/10/2016.
 */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.MyViewHolder> {

    private List<AlertModel> alertModelList;
    public AlertAdapter(ArrayList<AlertModel> alertModelList)
    {
        this.alertModelList = alertModelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alert_item_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
       AlertModel alertModel = alertModelList.get(position);

        holder.alert_msg_code.setText(alertModel.alert_category);
        holder.alert_msg.setText(alertModel.alert_message);

        if(alertModel.alert_category.contains("P"))
        {
            holder.img.setImageResource(R.drawable.winch_blue);
        }else if(alertModel.alert_category.contains("A"))
        {
            holder.img.setImageResource(R.drawable.bell_yellow);
        }
    }

    @Override
    public int getItemCount() {
        return alertModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView alert_msg, alert_msg_code, logTime;
        public ImageView img;

        public MyViewHolder(View view) {
            super(view);
            img = (ImageView) view.findViewById(R.id.img);
            alert_msg_code = (TextView) view.findViewById(R.id.alert_msg_code);
            alert_msg = (TextView) view.findViewById(R.id.alert_msg);
        }
    }

}
