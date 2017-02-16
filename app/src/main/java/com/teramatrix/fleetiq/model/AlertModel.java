package com.teramatrix.fleetiq.model;

/**
 * Created by arun.singh on 6/10/2016.
 */
public class AlertModel {

    public String alert_message;
    public String log_time;
    public String alert_category;

    public AlertModel(String alert_category, String alert_message)
    {
        this.alert_category = alert_category;
        this.alert_message = alert_message;
    }
}
