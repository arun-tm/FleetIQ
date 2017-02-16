package com.teramatrix.fleetiq.controller;

import android.content.Context;
import android.util.Log;

import com.teramatrix.fleetiq.Utils.SPUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * ...
 * @author Mohsin Khan
 * @date May 17 2016
 */
public class RESTClient {



//    GCM Server API Key = AIzaSyA7JfopI53LYvrcU-tfoAT1trWGNM6UQOU
//      GCM Sender ID = 206015261347
    public static final String LOGIN = "ThirdPartyApplicationDevelopment/oauth/token";
    public static final String DEVICE_REGISTRATION  = "ThirdPartyApplicationDevelopment/device/get/vehicle/registration";
    public static final String DEVICE_INFO = "ThirdPartyApplicationDevelopment/device/get/users/vehicles";
    public static final String NEW_DEVICES_LIST = "ThirdPartyApplicationDevelopment/get/device/by/user";


    public static final String DEVICE_LOCATION_DATA = "ThirdPartyApplicationDevelopment/performance/device/get/all/servicestatus";
    public static final String DEVICE_LOCATION_HISTORY_DATA = "ThirdPartyApplicationDevelopment/vehicle/calculate/trips";
    public static final String DEVICE_DATA = "ThirdPartyApplicationDevelopment/performance/status/device/get/all";

    private static OkHttpClient client = new OkHttpClient();


    private static MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");


    public static Call Login(Context context, String body, Callback callback) {
        Request request = new Request.Builder().url(new SPUtils(context).getRestEndPoint() + LOGIN)
                .post(RequestBody.create(mediaType, body)).build();

        Call call = client.newCall(request);

        call.enqueue(callback);
        printRequest(request);
        return call;
    }







    public static Call DeviceInfo(Context context, String body, Callback callback) {
        Request request = new Request.Builder().url(new SPUtils(context).getRestEndPoint() + DEVICE_INFO)
                .post(RequestBody.create(mediaType, body)).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        printRequest(request);
        return call;
    }



    public static Call getDeviceData(Context context, String body, Callback callback) {
        Request request = new Request.Builder().url(new SPUtils(context).getRestEndPoint() + DEVICE_DATA)
                .post(RequestBody.create(mediaType, body)).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        printRequest(request);
        return call;
    }

    public static Call getLocationData(Context context, String body, Callback callback) {
        Request request = new Request.Builder().url(new SPUtils(context).getRestEndPoint() + DEVICE_LOCATION_DATA)
                .post(RequestBody.create(mediaType, body)).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        printRequest(request);
        return call;
    }

    public static Call getLocationHstoryData(Context context, String body, Callback callback) {
        Request request = new Request.Builder().url(new SPUtils(context).getRestEndPoint() + DEVICE_LOCATION_HISTORY_DATA)
                .post(RequestBody.create(mediaType, body)).build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Call call = client.newCall(request);

        call.enqueue(callback);
        printRequest(request);
        return call;
    }

    public static Call getNewOBDDevices(Context context, String body, Callback callback) {
        Request request = new Request.Builder().url(new SPUtils(context).getRestEndPoint() + NEW_DEVICES_LIST)
                .post(RequestBody.create(mediaType, body)).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        printRequest(request);
        return call;
    }
    public static Call DeviceRegistration(Context context, String body, Callback callback) {
        Request request = new Request.Builder().url(new SPUtils(context).getRestEndPoint() + DEVICE_REGISTRATION)
                .post(RequestBody.create(mediaType, body)).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        printRequest(request);
        return call;
    }

    /**
     * Method will print Http Request body to the LogCat Error
     * @param request pass the request to print its body
     */
    private static void printRequest(Request request) {
        Log.e("-----------", "-------------------------------------------------------------------------");
        try {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            Log.e("WEB_SERVICE", "BODY \t-> " + buffer.readUtf8());
            Log.e("WEB_SERVICE", "URL\t-> " + request.url().toString());
            Log.e("-----------", "-------------------------------------------------------------------------");
        } catch (IOException|StringIndexOutOfBoundsException e) {
            Log.e("WEB_SERVICE", e.getMessage());
        }
    }
}
