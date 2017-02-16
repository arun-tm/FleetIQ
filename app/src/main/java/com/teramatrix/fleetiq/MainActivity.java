package com.teramatrix.fleetiq;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.app.Dialog;
import com.teramatrix.fleetiq.JLatex.core.AjLatexMath;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.MyAnnotation;
import com.teramatrix.fleetiq.Utils.SPUtils;
import com.teramatrix.fleetiq.Utils.TextViewAnnotation;
import com.teramatrix.fleetiq.fragments.AllParametersFragment;
import com.teramatrix.fleetiq.fragments.EnginePerformanceFragment;
import com.teramatrix.fleetiq.fragments.FuelAnalyticFragment;
import com.teramatrix.fleetiq.fragments.HomeFragment;
import com.teramatrix.fleetiq.fragments.MapFragment;
import com.teramatrix.fleetiq.fragments.RegisterFragment;
import com.teramatrix.fleetiq.fragments.VehicleHealthFragment;
import com.teramatrix.fleetiq.model.OBDVehicelInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

import static java.lang.Boolean.getBoolean;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    MyAnnotation myAnnotation = new MyAnnotation();

    @TextViewAnnotation(value = 10)
    private int myAnnotationValue;

    public static final String MAIN_BACK_STACK = "main";
    private NavigationView navigationView;
    private android.support.v4.app.Fragment openedFragment;
    android.support.v7.app.AlertDialog dialog;
    BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!GeneralUtilities.isConnected(context)) {
                    NoNetworkMessage();
                } else {
                    dismisNetworkErrorMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            myAnnotation.bind(this);


            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            //Set Account Selection Listener in navigation Header
            navigationView.getHeaderView(0).findViewById(R.id.account_selection).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        setAccountSelectionMenu(v.getTag());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            AjLatexMath.init(this);
            registerReceiver(networkChangeReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
            GeneralUtilities.loadFragment(this, new HomeFragment());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAccountSelectionMenu(Object actionTag) throws Exception {
        if (actionTag == null || ((String) actionTag).equalsIgnoreCase("close")) {
            navigationView.findViewById(R.id.h_layout).setVisibility(View.VISIBLE);
            navigationView.getHeaderView(0).findViewById(R.id.account_selection).setTag("open");
            ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.img_drop_down)).setImageResource(R.mipmap.ic_arrow_drop_up_white_24dp);
        } else if (((String) actionTag).equalsIgnoreCase("open")) {
            navigationView.findViewById(R.id.h_layout).setVisibility(View.GONE);
            navigationView.getHeaderView(0).findViewById(R.id.account_selection).setTag("close");
            ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.img_drop_down)).setImageResource(R.mipmap.ic_arrow_drop_down_white_24dp);
        }
    }

    public void setOpenedFragment(android.support.v4.app.Fragment fragment) {
        this.openedFragment = fragment;
    }

    public android.support.v4.app.Fragment getOpenedFragment() {
        return openedFragment;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(this, Setting.class));

            return true;
        } else if (id == R.id.action_logout) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
            builder.setMessage("Are you sure you want to logout ?");
            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //Logout User by clearing Stored Details
                    SPUtils spUtils = new SPUtils(MainActivity.this);
                    boolean remember_me_status =spUtils.getBoolean(SPUtils.REMEBER_ME_STATUS);
                    String user_id = spUtils.getString(SPUtils.USER_ID);
                    spUtils.clearPreferences();
                    spUtils.setValue(SPUtils.REMEBER_ME_STATUS,remember_me_status);
                    spUtils.setValue(SPUtils.USER_ID ,user_id);
                    finish();
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setCancelable(false);
            builder.show();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        try {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
            /*DashboardFragment dashboardFragment = new DashboardFragment();
            GeneralUtilities.loadFragment(this, dashboardFragment);*/
            } else if (id == R.id.nav_engine_performance) {
                EnginePerformanceFragment enginePerformanceFragment = new EnginePerformanceFragment();
                GeneralUtilities.loadFragment(this, enginePerformanceFragment);
            } else if (id == R.id.nav_gallery) {
                FuelAnalyticFragment fuelAnalyticFragment = new FuelAnalyticFragment();
                GeneralUtilities.loadFragment(this, fuelAnalyticFragment);

            } else if (id == R.id.nav_slideshow) {
                VehicleHealthFragment tyrePressureFragment = new VehicleHealthFragment();
                GeneralUtilities.loadFragment(this, tyrePressureFragment);

            } else if (id == R.id.nav_manage) {
                MapFragment mapFragment = new MapFragment();
                GeneralUtilities.loadFragment(this, mapFragment);

//                startActivity(new Intent(MainActivity.this,Map.class));
            } else if (id == R.id.nav_all_parameter) {
                AllParametersFragment allParametersFragment = new AllParametersFragment();
                GeneralUtilities.loadFragment(this, allParametersFragment);

            } else if (id == R.id.nav_register_vehicle) {
                RegisterFragment registerFragment = new RegisterFragment();
                GeneralUtilities.loadFragment(this, registerFragment);
            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            setAccountSelectionMenu("open");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    public void setAccountInfo(String title, String subtitle) {
        ((TextView) findViewById(R.id.account_title)).setText(title);
        ((TextView) findViewById(R.id.account_subtitle)).setText(subtitle);
    }

    public void setAccountsInNavigationDrawer(final ArrayList<OBDVehicelInfo> accountsInNavigationDrawer) {
        try {

            if (accountsInNavigationDrawer.size() == 0)
                ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.img_drop_down)).setVisibility(View.GONE);
            else
                ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.img_drop_down)).setVisibility(View.VISIBLE);

            View someView = navigationView.getHeaderView(0).findViewById(R.id.h_layout);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                    someView.getLayoutParams();


            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, r.getDisplayMetrics());

            params.height = accountsInNavigationDrawer.size() * (int) px;
            someView.setLayoutParams(params);

            LinearLayout account_list_parent_view = (LinearLayout) navigationView.getHeaderView(0).findViewById(R.id.account_list_parent_view);
            account_list_parent_view.removeAllViews();
            for (OBDVehicelInfo obdVehicelInfo : accountsInNavigationDrawer) {
                View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_accout_row, null);
                TextView txt_modelname = (TextView) v.findViewById(R.id.txt_modelname);
                TextView txt_model_registration = (TextView) v.findViewById(R.id.txt_model_registration);
                final OBDVehicelInfo obdVehicelInfo_local = obdVehicelInfo;
                txt_modelname.setText(obdVehicelInfo.model_code);
                txt_model_registration.setText(obdVehicelInfo.registration_no);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            //Close Navigation menue
                            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                            drawer.closeDrawer(GravityCompat.START);
                            setAccountSelectionMenu("open");


                            Intent intent = new Intent();
                            String device_id = obdVehicelInfo_local.device_id;
                            intent.putExtra("selected_bd_device_id", device_id);
                            intent.setAction("ACTION_OBD_SOURCE_CHANGED");
                            sendBroadcast(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                account_list_parent_view.addView(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NoNetworkMessage() throws Exception {
        android.support.v7.app.AlertDialog.Builder alertDialog = alertDialog = new android.support.v7.app.AlertDialog.Builder(this, R.style.AlertDialogCustom);
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setTitle("No internet");
        alertDialog.setMessage("This feature requires an active internet connection. Please check your internet setting and try again.");
        dialog = alertDialog.create();
        dialog.show();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            }
        });
    }

    public void dismisNetworkErrorMessage() throws Exception {
        if (dialog != null && dialog.isShowing()) {
            //Dismis Shown Dialog,after user has enabled network
            dialog.dismiss();

            //send API call to get Latest Data
            sendBroadcast(new Intent("ACTION_DASHBOARD_DATA_UPDATE"));
        }
    }

}

