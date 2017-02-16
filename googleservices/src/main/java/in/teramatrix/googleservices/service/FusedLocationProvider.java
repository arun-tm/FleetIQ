package in.teramatrix.googleservices.service;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * At the time, <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi#top_of_page">
 * Fused Location Provider Api</a> is the best way to get current location update. This class is a simple illustration of this API.
 * The main entry point for interacting with the fused location provider is {@link GoogleApiClient}.
 * @author Mohsin Khan
 * @date 15/4/2016
 */
@SuppressWarnings("unused")
public class FusedLocationProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /**
     * To initialize {@link GoogleApiClient}
     */
    private Context context;

    /**
     * <b>The main entry point for Google Play services integration.</b>
     * GoogleApiClient is used with a variety of static methods. Some of these methods require that GoogleApiClient be connected,
     * some will queue up calls before GoogleApiClient is connected; Here we need to be connected to use
     * <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi">Fused Location API.</a>
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * A data object that contains quality of service parameters for requests to the
     * <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi">Fused Location API.</a>
     * {@link LocationRequest} objects are used to request a quality of service for location updates from the
     * <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi">Fused Location API.</a>
     */
    private LocationRequest mLocationRequest;

    private LocationListener mLocationListener;

    /**
     * Set the priority of the request.
     * Use with a priority constant such as PRIORITY_HIGH_ACCURACY. No other values are accepted.
     * The priority of the request is a strong hint to the LocationClient for which location sources to use.
     * For example, PRIORITY_HIGH_ACCURACY is more likely to use GPS, and PRIORITY_BALANCED_POWER_ACCURACY is more likely to use
     * WIFI & Cell tower positioning, but it also depends on many other factors (such as which sources are available) and
     * is implementation dependent.
     */
    private int priority;

    /**
     * Set the desired interval for active location updates, in milliseconds.
     * The location client will actively try to obtain location updates for your application at this interval,
     * so it has a direct influence on the amount of power used by your application. Choose your interval wisely.
     */
    private long interval;

    /**
     * Explicitly set the fastest interval for location updates, in milliseconds.
     * This controls the fastest rate at which your application will receive location updates, which might be faster than setInterval(long)
     * in some situations (for example, if other applications are triggering location updates).
     * This allows your application to passively acquire locations at a rate faster than it actively acquires locations, saving power.
     */
    private long fastestInterval;

    /**
     * Used with setPriority(int) to request the most accurate locations available.
     * This will return the finest location available.
     */
    public static final int PRIORITY_HIGH_ACCURACY = 100;

    /**
     * Used with setPriority(int) to request "block" level accuracy.
     * Block level accuracy is considered to be about 100 meter accuracy. Using a coarse accuracy such as this often consumes less power.
     */
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102;

    /**
     * Used with setPriority(int) to request "city" level accuracy.
     * City level accuracy is considered to be about 10km accuracy. Using a coarse accuracy such as this often consumes less power.
     */
    public static final int PRIORITY_LOW_POWER = 104;

    /**
     * Used with setPriority(int) to request the best accuracy possible with zero additional power consumption.
     * No locations will be returned unless a different client has requested location updates in which case this request will act as a passive listener to those locations.
     */
    public static final int PRIORITY_NO_POWER = 105;

    public FusedLocationProvider() {
        //Default Settings
        this.priority = PRIORITY_HIGH_ACCURACY;
        this.interval = 1000 * 10;
        this.fastestInterval = 1000 * 10;
    }

    public FusedLocationProvider(Context context) {
        this.context = context;
        //Default Settings
        this.priority = PRIORITY_HIGH_ACCURACY;
        this.interval = 1000 * 10;
        this.fastestInterval = 1000 * 10;
    }

    public FusedLocationProvider(Context context, int priority, long interval, long fastestInterval) {
        this.context = context;
        //Default Settings
        this.priority = priority;
        this.interval = interval;
        this.fastestInterval = fastestInterval;
    }

    public FusedLocationProvider setContext(Context context) {
        this.context = context;
        return this;
    }

    public FusedLocationProvider setLocationListener(LocationListener mLocationListener) {
        this.mLocationListener = mLocationListener;
        return this;
    }

    public FusedLocationProvider setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public FusedLocationProvider setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    public FusedLocationProvider setFastestInterval(long fastestInterval) {
        this.fastestInterval = fastestInterval;
        return this;
    }

    public FusedLocationProvider start() {
        if (context == null)
            throw new NullPointerException("Context can not be null");

        if (mLocationListener == null)
            throw new NullPointerException("LocationListener can not be null");

        mLocationRequest = new LocationRequest()
                .setInterval(interval)
                .setFastestInterval(fastestInterval)
                .setPriority(priority);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
        }

        return this;
    }

    public void stop() {
        if (mGoogleApiClient != null && mLocationListener != null)
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
        mLocationListener.onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("Fused", "onConnectionSuspended()");
        stop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("Fused", "onConnectionFailed() : " + connectionResult.toString());
        stop();
    }
}