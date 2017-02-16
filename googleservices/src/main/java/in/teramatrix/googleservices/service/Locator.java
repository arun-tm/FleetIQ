package in.teramatrix.googleservices.service;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Timer;
import java.util.TimerTask;

import in.teramatrix.googleservices.R;
import in.teramatrix.googleservices.util.CoordinateUtilities;
import in.teramatrix.googleservices.util.GoogleMapUtilities;

/**
 * This class is designed to show current location on {@link GoogleMap}. Here A {@link Marker} and a {@link Circle} will be plotted on the map.
 * Marker will show the position and circle will show accuracy of location. To animate circle according to increment and decrement in location
 * accuracy, a method is defined that is {@code animateCircle()}. There is two methods to make this class work, these are {@code locate()} and
 * {@code locateMe()}. In {@code locate()}, you can pass any location but on the other hand, in {@code lcoateMe()}, {@link FusedLocationProvider}
 * will be used to get location and only current location will shown on the map. {@code locateMe()} will return the running instance of
 * {@link FusedLocationProvider} so you can stop location updates anytime by calling {@code stop()} method of {@link FusedLocationProvider} class
 * or to stop locating, just call {@code stop()} of this class. It will remove {@link Marker} and {@link Circle} also.
 * @author Mohsin Khan
 * @date 4/13/2016
 */
@SuppressWarnings("unused")
public class Locator {
    /**
     * On which the location marker will be plotted
     */
    private GoogleMap map;

    /**
     * A single instance of marker, to show the location
     */
    private Marker marker;

    /**
     * Specially used to present accuracy of location
     */
    private Circle circle;

    /**
     * If calculating distance from latest location, this will be the factor from the distance will be calculated.
     */
    private Location recent;

    /**
     * To fetch current location of device. This will only be initialized when {@code locateMe()} method is called
     */
    private FusedLocationProvider provider;

    /**
     * Accuracy layer ({@link Circle} will not be visible if this is false.
     */
    private boolean accuracyLayer;

    /**
     * To be filled in circle that will show accuracy
     */
    private String fillColor;

    /**
     * It is border color of the circle that will show accuracy
     */
    private String strokeColor;

    /**
     * Location marker that will be shown on the map
     */
    private int markerIcon;

    /**
     * Default constructor of the class
     * @param map on which the current location will be displayed
     */
    public Locator(GoogleMap map) {
        this.map = map;
        //Default Setting
        this.fillColor = "#3273b7ff";
        this.strokeColor = "#4487f2";
        this.accuracyLayer = true;
        this.markerIcon = R.drawable.ic_current_location;
    }

    public Locator setAccuracyLayer(boolean accuracyLayer) {
        this.accuracyLayer = accuracyLayer;
        return this;
    }

    public Locator setFillColor(String fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public Locator setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public Locator setMarkerIcon(int markerIcon) {
        this.markerIcon = markerIcon;
        return this;
    }

    /**
     * This will plot current location {@link Marker} on the {@link GoogleMap} using {@link FusedLocationProvider}
     * @param context to initialize {@link FusedLocationProvider}
     * @return running instance of {@link FusedLocationProvider} to stop updates later or whenever you want.
     */
    public FusedLocationProvider locateMe(Context context) {
        provider = new FusedLocationProvider()
                .setContext(context)
                .setPriority(FusedLocationProvider.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000 * 5)
                .setFastestInterval(1000 * 5)
                .setLocationListener(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        locate(location);
                    }
                })
                .start();
        return provider;
    }

    /**
     * This method will maintain a {@link Marker} and a {@link Circle} on the map. Every time a location is passed in this method,
     * and the method will remove previous {@link Marker} & {@link Circle} and plot a new {@link Marker} and {@link Circle} on the new {@link Location}
     * you passed in the parameter.
     * @param location where current location marker will be plotted
     */
    public void locate(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (marker == null) {
                if (map != null) {
                    marker = GoogleMapUtilities.addMarker(map, latLng, markerIcon, true);
                    if (accuracyLayer)
                    circle = map.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(location.getAccuracy())
                            .strokeColor(Color.parseColor(strokeColor))
                            .strokeWidth(0.8f)
                            .fillColor(Color.parseColor(fillColor)));
                    GoogleMapUtilities.animateCamera(map, latLng, 15);
                }
            } else {
                if (!CoordinateUtilities.isEqual(location, recent)) {
                    marker.setPosition(latLng);
                    if(accuracyLayer) {
                        circle.setCenter(latLng);
                        animateCircle(circle, Math.round(location.getAccuracy()));
                    }
                }
            }
            recent = location;
        }
    }

    /**
     * Method will generate animation like effects by increasing/decreasing the value of {@link Circle} radius using
     * Android's {@link Handler}.
     * One more thing to consider that here I'm checking if new radius is just one meter up/down with the existing
     * radius then this animation will not work. It will be stable in that condition.
     * @param c circle to be animated
     * @param r new radius value
     */
    private void animateCircle(final Circle c, final double r) {
        c.setRadius(Math.round(c.getRadius()));
        if (c.getRadius() != r && c.getRadius() != r - 1 && c.getRadius() != r + 1) {
            //Calculating difference
            final int d = (int) Math.abs(c.getRadius() - r);
            //Calculating speed of circle resizing/scaling
            final int s = (1000 + d) / d;
            final Handler h = new Handler();
            h.post(new Runnable() {
                public void run() {
                    c.setRadius((c.getRadius() < r) ? c.getRadius() + 1 : c.getRadius() - 1);
                    if (c.getRadius() == r) {
                        h.removeCallbacks(this);
                        return;
                    }
                    h.postDelayed(this, s);
                }
            });
        }
    }

    /**
     * Method will generate animation like effects by increasing/decreasing the value of {@link Circle} radius using
     * Java's {@link Timer}.
     * One more thing to consider that here I'm checking if new radius is just one meter up/down with the existing
     * radius then this animation will not work. It will be stable in that condition.
     * @param cxt context to execute on main thread
     * @param c circle to be animated
     * @param r new radius value
     */
    private void animateCircle(final Context cxt, final Circle c, final double r) {
        c.setRadius(Math.round(c.getRadius()));
        if (c.getRadius() != r && c.getRadius() != r - 1 && c.getRadius() != r + 1) {
            //Calculating difference
            final int d = (int) Math.abs(c.getRadius() - r);
            //Calculating speed of circle resizing
            final int s = (1000 + d) / d;
            final Timer t = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (cxt != null)
                        ((Activity)cxt).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                c.setRadius((c.getRadius() < r) ? c.getRadius() + 1 : c.getRadius() - 1);
                                if (c.getRadius() == r)  {
                                    t.cancel();
                                }
                            }
                        });
                }
            };
            t.schedule(timerTask, 0, 50);
        }
    }

    /**
     * @param circle circle to be animated
     */
    private void animateCircle(final Circle circle) {
        ValueAnimator vAnimator = new ValueAnimator();
        vAnimator.setRepeatCount(ValueAnimator.INFINITE);
        vAnimator.setRepeatMode(ValueAnimator.RESTART);  /* PULSE */
        vAnimator.setIntValues(0, 100);
        vAnimator.setDuration(1000);
        vAnimator.setEvaluator(new IntEvaluator());
        vAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                circle.setRadius(animatedFraction * 100);
            }
        });
        vAnimator.start();
    }

    /**
     * To stop monitoring current location. This method will stop Location Provider that is {@link FusedLocationProvider}
     * and will remove current location {@link  Marker} and accuracy {@link Circle}.
     */
    public void stop() {
        if (marker != null) marker.remove();
        if (circle != null) circle.remove();
        if (provider != null) provider.stop();
    }
}
