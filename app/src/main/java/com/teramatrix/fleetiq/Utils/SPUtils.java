package com.teramatrix.fleetiq.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * To store user details in shared preferences. At startup, we will not prompt to user to login if a user is already logged in
 * So this login details will be stored in android shared preferences
 *
 * @author Mohsin Khan
 * @date 16 May 2016
 */

@SuppressWarnings("unused")
public class SPUtils {
    /**
     * SharedPreferences instance and its editor to get preferences in writable mode and to store
     * information and app preferences.
     */
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedpreferences;

    /**
     * Name of the shared preferences, this should be your application name
     */
    public static final String PREFERENCES = "flint";

    /*
     * Following fields are related to the user's information returned when a user get registered
     * first time at LoginActivity.class. In fact, according to the application, a user can't logout,
     * so this information will be stored only once in the whole application life cycle
     */
    public static final String ACCESS_TOKEN = "access_token";
    public static final String USER_KEY = "userKey";
    public static final String ACCESS_KEY = "access_key";
    public static final String USER_ID = "user_id";
    public static final String DEVICE_ID = "device_id";
    public static final String VEHICLE_MODEL = "model";
    public static final String VEHICLE_REGISTRATION_NUMBER = "reg_number";

    public static final String VEHICLE_ADDRESS = "address";
    public static final String VEHICLE_USERNAME = "user_name";
    public static final String VEHICLE_DRIVER_NAME = "driver_name";
    public static final String VEHICLE_TYPE = "vehicle_type";
    public static final String VEHICLE_CONDITION = "condition";
    public static final String VEHICLE_ENGINE_NUMBER = "engine_number";
    public static final String VEHICLE_CHASIS_NUMBER = "chasis_number";
    public static final String VEHICLE_STATUS = "vehicle_status";
    public static final String VEHICLE_FUEL_TYPE = "fuel_type";
    public static final String VEHICLE_PURCHASE_DATE = "purchase_date";
    public static final String VEHICLE_COLOR_CODE = "color_code";
    public static final String VEHICLE_OBD_DEVICE_NAME = "vehicle_obd_device_name";

    public static final String IS_SCAN_RESULT_SHOWN = "scan_result";

    public static final String TRIP_START_LATITUDE = "trip_start_latitude";
    public static final String TRIP_START_LONGITUDE = "trip_start_longitude";
    public static final String REMEBER_ME_STATUS = "remember_me";



    public static final String APP_NAME = "appName";
    /**
     * To keep Logout information in shared preferences
     */
    public static final String LOGOUT = "logout";
    /**
     * To keep API address
     */
    public static final String API_END_POINT = "api";
    /**
     * If user accepts all terms and conditions at the time of login then this bit will be true in shared preferences
     */
    public static final String TERMS_AND_CONDITIONS = "termsAndConditions";

    public SPUtils(Context context) {
        if (context != null)
            sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * @return value for the specified tag stored in shared preferences.
     */
    public String getString(String tag) {
        return sharedpreferences.getString(tag, "");
    }

    /**
     * @return value for the specified tag stored in shared preferences.
     */
    public Boolean getBoolean(String tag) {
        return sharedpreferences.getBoolean(tag, false);
    }

    /**
     * @return value for the specified tag stored in shared preferences.
     */
    public String getDate(String tag) {
        return sharedpreferences.getString(tag, "1 Jan 2015 00:00:00");
    }

    /**
     * It will store key value pair in the shared preferences
     *
     * @param tag   name of the key
     * @param value for the key.
     */
    public void setValue(String tag, String value) {
        editor = sharedpreferences.edit();
        editor.putString(tag, value);
        editor.apply();
    }

    /**
     * It will store key value pair in the shared preferences
     *
     * @param tag   name of the key
     * @param value true/false
     */
    public void setValue(String tag, boolean value) {
        editor = sharedpreferences.edit();
        editor.putBoolean(tag, value);
        editor.apply();
    }

    /**
     * Method will store all information of user with their keys
     *
     * @param user a hash map to get users information with keys
     */
    public void storeUsersInformation(HashMap<String, String> user) {
        editor = sharedpreferences.edit();
        //Obtaining random keys form the HashMap
        ArrayList<String> keys = new ArrayList<>(user.keySet());
        for (String key : keys) {
            editor.putString(key, user.get(key));
        }
        editor.apply();
    }

    /**
     * Method flushPreferences() will remove complete data stored in the shared preferences.
     * So it may be happen that application will start as first time.
     */
    public void clearPreferences() {
        editor = sharedpreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * @return application name to be added with SOAP request while authenticating user
     */
    public String getAppName() {
        return sharedpreferences.getString(APP_NAME, "FLINT");
    }

    /**
     * @return security token to be added with HTTP request
     */
    public String getToken() {
        return sharedpreferences.getString(ACCESS_TOKEN, "");
    }

    public String getAccessKey() {
        return sharedpreferences.getString(ACCESS_KEY, "");
    }

    public String getUserKey() {
        return sharedpreferences.getString(USER_KEY, "");
    }

    public String getUserID() {
        return sharedpreferences.getString(USER_ID, "");
    }

    public String getDeviceID() {
        return sharedpreferences.getString(DEVICE_ID, "");
    }


    public String getVehicleModel() {
        return sharedpreferences.getString(VEHICLE_MODEL, "");
    }

    public String getVehicleRegistrationNumber() {
        return sharedpreferences.getString(VEHICLE_REGISTRATION_NUMBER, "");
    }


    /**
     * @return default SOAP Api address
     */
    public String getSoapEndPoint() {
        //Local Auth Engine Access Link
        //return sharedpreferences.getString(API_END_POINT, "http://192.168.1.97/");

        //Live Auth Engine Access Link
        return sharedpreferences.getString(API_END_POINT, "http://kunal-wa16b7he.cloudapp.net/");
    }

    /**
     * @return default API address or stored API address
     */
    public String getRestEndPoint() {
        /*return sharedpreferences.getString(API_END_POINT, "http://23.99.122.164:8081/");*/
//      return sharedpreferences.getString(API_END_POINT, "http://192.168.1.97:4444/");

        //Local URL
//        return sharedpreferences.getString(API_END_POINT, "http://192.168.1.97:4444/");

        //Google Play Store APK version url
        return sharedpreferences.getString(API_END_POINT, "http://180.149.46.100:4444/");
    }
}