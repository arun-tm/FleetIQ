package com.teramatrix.fleetiq;

import android.app.Application;
import android.content.Intent;
import android.view.ViewConfiguration;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.lang.reflect.Field;

/**
 * Created by arun.singh on 7/1/2016.
 */
public class Flint extends Application {

    private Tracker mTracker;

    public void onCreate() {
        super.onCreate();
        makeActionOverflowMenuShown();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        Intent intent = new Intent (this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity(intent);
        System.exit(1); // kill off the crashed app
    }
    /**
     * Sometimes it happens that option menu will not visible on the action bar overflow.
     * Those menus can be achieved by clicking on hardware button for options.
     * But our system requirements are to show these option menus in action bar overflow(three dots)
     * This method will do this.
     */
    private void makeActionOverflowMenuShown() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        Tracker mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.enableExceptionReporting(true);

        return mTracker;
    }

    public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getDefaultTracker();

            t.send(new HitBuilders.ExceptionBuilder()
                            .setDescription(
                                    new StandardExceptionParser(this, null)
                                            .getDescription(Thread.currentThread().getName(), e))
                            .setFatal(false)
                            .build()
            );
        }
    }


}
