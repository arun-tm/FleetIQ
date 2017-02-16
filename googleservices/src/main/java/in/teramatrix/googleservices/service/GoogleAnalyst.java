package in.teramatrix.googleservices.service;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * At the moment, this class is only facilitated with only four major Google Analytics services. That means class is able to
 * send screen names, actions, custom dimensions and exceptions. To execute the operation, you have to pass a {@link Tracker} in every static method.
 * Here I'm giving the definition of the method that will help you to get {@link Tracker}, just place it in {@link Application} class.
 *
 * <pre>
 * private Tracker mTracker;
 * synchronized public Tracker getDefaultTracker() {
 *      if (mTracker == null) {
 *          GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
 *          // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
 *          mTracker = analytics.newTracker(R.xml.global_tracker);
 *          mTracker.enableExceptionReporting(true);
 *      }
 *      return mTracker;
 * }
 * </pre>
 * <br/>
 * For more details : <a href="https://developers.google.com/analytics/devguides/collection/android/v4/#set-up-your-project">Add Analytics to Your Android App</a>
 *
 * @author Mohsin Khan
 * @date 3/23/2016
 */
@SuppressWarnings("unused")
public class GoogleAnalyst {

    /**
     * This method will send the name of current visible screen to the analytics.
     * <br/>
     * See <a href="https://developers.google.com/analytics/devguides/collection/android/v4/screens#overview">Screens</a>
     * @param screenName name of the screen that will be on Google Analytics
     */
    public static void sendScreenName(Tracker mTracker , String screenName) {
        if (mTracker != null) {
            mTracker.setScreenName(screenName);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    /**
     * This method will send action/event to the analytics.
     * <br/>
     * See <a href="https://developers.google.com/analytics/devguides/collection/android/v4/events#overview">Event Tracking</a>
     * @param category name of event/action category that will be on Google Analytics
     * @param action name of action like what is user doing right now for example "watching gallery"
     */
    public static void sendEvent(Tracker mTracker, String category, String action) {
        if (mTracker != null) {
            mTracker.send(
                    new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .build()
            );
        }
    }

    /**
     * This method will send screen name and custom dimensions to the analytics. To accomplish task just pass a {@link HashMap<>}
     * of key value pair. For example consider following, here we are sending user's information to the analytics.
     * <br/><br/>
     * {@code Hashmap<String, String> dimensions = new Hashmap<String, String>();}
     * <br/>
     * {@code dimensions.put("&cd1", userId);}
     * <br/>
     * {@code dimensions.put("&cd2", email);}
     * <br/>
     * {@code dimensions.put("&cd3", phone);}
     * <br/>
     * {@code dimensions.put("&cd4", timezone);}
     * <br/>
     * For more details : <a href="https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#cd_">Custom Dimension</a>
     * @param screenName name of the screen that will be on Google Analytics
     * @param dimensions a map of key(custom dimension index)-value pair
     */
    public static void sendCustomDimension(Tracker mTracker, String screenName, HashMap<String, String> dimensions) {
        if (mTracker != null) {
            mTracker.setScreenName(screenName);

            //Adding all keys and their values to tracker
            ArrayList<String> keys = new ArrayList<>(dimensions.keySet());
            for (String key : keys)
                mTracker.set(key, dimensions.get(key));

            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    /**
     * This method will send {@link Exception} to google analytics.
     * @param context to be passed in {@link StandardExceptionParser}
     * @param mTracker default tracker instance
     * @param e exception to be sent
     */
    public static void sendException(Context context, Tracker mTracker, Exception e) {
        if (mTracker != null && e != null) {
            String description = new StandardExceptionParser(context, null).getDescription(Thread.currentThread().getName(), e);
            mTracker.send(new HitBuilders.ExceptionBuilder()
                            .setDescription(description)
                            .setFatal(false)
                            .build());
        }
    }

    /**
     * This will invoke a method of {@link Application} class to get default tracker. Method name is hardcoded here.
     * @param application the instance of {@link Application} class, keep in mind it should be also setup in manifest file as {@code <application android:name=".YourApplicationClass"> ... </application>}
     * @return initialized {@link Tracker}
     */
    @Deprecated
    @SuppressWarnings({"TryWithIdenticalCatches", "NullArgumentToVariableArgMethod"})
    private static Tracker getTracker(Class<?> application) {
        try {
            Method method = application.getMethod("getDefaultTracker", null);
            return (Tracker) method.invoke(application.newInstance());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
