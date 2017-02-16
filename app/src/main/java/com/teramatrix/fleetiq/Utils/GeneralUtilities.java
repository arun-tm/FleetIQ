package com.teramatrix.fleetiq.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.teramatrix.fleetiq.MainActivity;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.fragments.HomeFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


/**
 * <pre>
 * Author       :   Mohsin Khan
 * Date         :   November 18 , 2015
 * Description  :   These are some general functions that will be useful in almost all classes.
 *                 Here I have created some methods to check intertern connectivity, check google services, to get screen height and width,
 *                 to get device IMEI number, to show alert dialog on different event etc. These all are common utilities.
 * </pre>
 */

@SuppressWarnings({"ConstantConditions", "deprecation", "unused"})
public class GeneralUtilities {
    /**
     * To perform some graphical operations and to get system services we need context
     * Like we are accessing CONNECTIVITY_SERVICE, TELEPHONY_SERVICE etc.
     */
    private Context context;

    /**
     * I'm showing progress bar. The progressbar will be inflated in a popup window
     * As we received any response the popup will be dismissed automatically
     */
    private PopupWindow popupWindow;


    public GeneralUtilities(Context context) {
        this.context = context;
    }

    /**
     * Method is using Connectivity Services to get Connectivity manager. that will find network informations
     * If we found connected state of any network that means we are connected to a network
     *
     * @return true if we are connected otherwise false
     */
    public Boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo i : info) {
                    if (i.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo i : info) {
                    if (i.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Mobile screen size may be vary so here is a method that is helpful to get decisions according
     * to the screen width.
     *
     * @return screen width in pixels
     */
    public int getScreenWidth() {
        int width;
        Point point = new Point();
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT < 13) {
            width = display.getWidth();
        } else {
            display.getSize(point);
            width = point.x;
        }
        return width;
    }

    /**
     * Mobile screen size may be vary so here is a method that is helpful to get decisions according
     * to the screen height.
     *
     * @return screen height in pixels
     */
    public int getScreenHeight() {
        Point point = new Point();
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT < 13) {
            return display.getHeight();
        } else {
            display.getSize(point);
            return point.y;
        }
    }

    /**
     * This method is calculating location of the view on mobile screen
     *
     * @param v whose location will be found
     * @return rectangle of the view's location
     */
    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException e) {
            return null;
        }
        Rect position = new Rect();
        position.left = loc_int[0];
        position.top = loc_int[1];
        position.right = position.left + v.getWidth();
        position.bottom = position.top + v.getHeight();
        return position;
    }

    /**
     * Method is getting Telephony Services to get Device's unique ID
     *
     * @return IMEI number of the device
     */
    public String getDeviceImei() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }


    /**
     * This method simply hide the virtual keyboard
     */
    public void hideKeyboard() {
        final AppCompatActivity activity = (AppCompatActivity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.getCurrentFocus() != null) {
                    ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((AppCompatActivity) context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("GeneralUtils", "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }


    /**
     * This method will hide the progressbar by dismissing the popup window
     * Just because we cant touch the UI from another thread, that's why I'm using
     * runOnUiThread()
     */
    public void stopProgressBar() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (popupWindow != null && popupWindow.isShowing())
                    popupWindow.dismiss();
            }
        });
    }


    public static String getCurrentUtcTime() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatGmt.format(new Date());
    }

    public static String getCurrentUtcTime2() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatGmt.format(new Date());
    }

    public static String getCurrentUtcTime3() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatGmt.format(new Date());
    }

    public static long convertDateStringToMilliseconds(String date_string, String formate) {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        try {
            Date date = sdf.parse(date_string);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String convertUtcTimeToIST(String UtcTime) {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        try {
            Date date = dateFormatGmt.parse(UtcTime);
            //Add 5:30 hours to UTC Time
            Date newDate = new Date(date.getTime() + TimeUnit.MINUTES.toMillis(330));
            String ist_string = dateFormatGmt.format(newDate.getTime());
            return ist_string;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String format_DateString_From_One_Pattern_To_Another_Pattern(String date_string, String current_pattern, String expected_pattern) {
        try {
            SimpleDateFormat dateFormat_current = new SimpleDateFormat(current_pattern);
            SimpleDateFormat dateFormat_expected = new SimpleDateFormat(expected_pattern);

            Date date = dateFormat_current.parse(date_string);

            return dateFormat_expected.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String format_DateString_From_Millisecond_To_Another_Pattern(String date_milliseconds, String expected_pattern) {
        try {
            long timeStamp = Long.parseLong(date_milliseconds);
            Date date = new Date(timeStamp);
            SimpleDateFormat dateFormat_expected = new SimpleDateFormat(expected_pattern);
            return dateFormat_expected.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String getTimeOffset(String starTimeInMilliseconds, String endTimeInMilliseconds) {

        long time_gap_in_milliseconds = Long.parseLong(endTimeInMilliseconds) - Long.parseLong(starTimeInMilliseconds);

        int seconds = (int) (time_gap_in_milliseconds / 1000) % 60;
        int minutes = (int) ((time_gap_in_milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((time_gap_in_milliseconds / (1000 * 60 * 60)) % 24);


        if (hours > 0 || minutes > 0)
            return hours +":"+minutes;
        /*if (hours > 0) {
            if (minutes > 0) {

                return hours + " h " + minutes + " min";

            }
            else
                return hours + " h";
        }else if(minutes>0)
        {
                return minutes + " min";
        }*/
        return " -- ";
    }

    public static String getDateTime(String timeZone, String dateFormat, int dayOffset) {
        DateFormat dateForma = new SimpleDateFormat(dateFormat);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, dayOffset);
        /*if(timeZone.equalsIgnoreCase("GMT"))
        {
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        }else if(timeZone.equalsIgnoreCase("IST"))
        {
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("IST"));
        }*/
//        long l = new Date().getTime() +(dayOffset*86400000 );
        return dateForma.format(cal.getTime());
    }


    public static String getCurrentIST_Time() {
        return convertUtcTimeToIST(getCurrentUtcTime3());
    }

    /**
     * Method will change the typeface of the textview to bold
     *
     * @param view only textview or edittext allowed
     */
    public void setBold(View view) {
        TextView textView = (TextView) view;
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }

    public static boolean isDataNotNull(String data) {
        if (data != null && !data.equalsIgnoreCase("null") && data.length() > 0)
            return true;
        else
            return false;
    }

    public static void setTextViewSize(Context context, TextView textView, float default_size_for_mdpi) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int dpi = metrics.densityDpi;
        if (dpi == 120) {
            //ldpi
//            default_size_for_mdpi = default_size_for_mdpi - 2;
        } else if (dpi == 160) {
            //mdpi

        } else if (dpi == 240) {
            //hdpi
            default_size_for_mdpi = default_size_for_mdpi - 4;
        } else if (dpi == 320) {
            //xdpi
            default_size_for_mdpi = default_size_for_mdpi + 2;
        } else if (dpi == 480) {
            //xxdpi
//            default_size_for_mdpi = default_size_for_mdpi+3;
        } else if (dpi == 640) {
            //xxxdpi
//            default_size_for_mdpi = default_size_for_mdpi+4;
        }
        System.out.println("dpi:" + dpi + " Applyed Size:" + default_size_for_mdpi);
        textView.setTextSize(default_size_for_mdpi);
    }

    public static void loadFragment(AppCompatActivity activity, Fragment fragment) {
        if (fragment instanceof HomeFragment)
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        else
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(MainActivity.MAIN_BACK_STACK)
                    .commit();
    }

    public static void setUpActionBar(Activity activity, Fragment fragment, String title, String subtitle) {

        AppCompatActivity a = (AppCompatActivity) activity;
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        a.setTitle(title);
        if (subtitle != null) {
            a.getSupportActionBar().setSubtitle(subtitle);
        } else
            a.getSupportActionBar().setSubtitle(null);

        if (fragment instanceof HomeFragment) {
//            a.getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_app_logo);
            a.getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_menu_white_24dp);
            a.getSupportActionBar().setHomeButtonEnabled(true);
            a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            a.getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_menu_white_24dp);
        }
    }

    public static String getRoundOffValue(float f) {
        String s = String.format("%.02f", f);
        return s;
    }

    public static String getRoundOffValue(String s, int precision) {
        if (s == null)
            return "N/A";
        try {

            if (!s.contains("."))
                return s;

            float value = Float.parseFloat(s);

            /*int scale = (int) Math.pow(10, precision);
            return (Math.round(value * scale) / scale) +"";*/

            if (precision == 1)
                return String.format("%.1f", value);
            else
                return String.format("%.02f", value);
        } catch (Exception e) {
            return s;
        }
    }

    public static String getDeviceImei(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


        return telephonyManager.getDeviceId();
    }

    public static int generateRandomNumber(int minimum, int maximum) {
        Random rn = new Random();
        int range = maximum - minimum + 1;
        int randomNum = rn.nextInt(range) + minimum;
        return randomNum;
    }

    public static String getMonth(int m) {
        switch (m) {
            case 0:
                return "Jan";
            case 1:
                return "Feb";
            case 2:
                return "Mar";
            case 3:
                return "Apr";
            case 4:
                return "May";
            case 5:
                return "Jun";
            case 6:
                return "Jul";
            case 7:
                return "Aug";
            case 8:
                return "Sep";
            case 9:
                return "Oct";
            case 10:
                return "Nov";
            case 11:
                return "Dec";
            default:
                return "";
        }
    }

    /*
    *
    *
    * */
    public static void getDatePeriodRange(String week_month_year, String today_date, int count) {

        String[] week_month_year_string = new String[12];
        if (week_month_year.equalsIgnoreCase("week")) {

//            for(int i =0;i<count;i++)
//            {

            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            for (int i = 0; i < 15; i++) {
                System.out.print("Start Date : " + c.getTime() + ", ");
                c.add(Calendar.DAY_OF_WEEK, 6);
                System.out.println("End Date : " + c.getTime());
                c.add(Calendar.DAY_OF_WEEK, 1);
            }

//            }


        } else if (week_month_year.equalsIgnoreCase("month")) {

        } else if (week_month_year.equalsIgnoreCase("year")) {

        }

    }
}
