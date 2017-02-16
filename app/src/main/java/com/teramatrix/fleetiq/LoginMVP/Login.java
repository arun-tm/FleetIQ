package com.teramatrix.fleetiq.LoginMVP;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.MainActivity;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.ImageUtils;
import com.teramatrix.fleetiq.Utils.MyAnnotation;
import com.teramatrix.fleetiq.Utils.SPUtils;
import com.teramatrix.fleetiq.Utils.TextViewAnnotation;
import com.teramatrix.fleetiq.controller.RESTClient;

import org.json.JSONObject;

import java.io.IOException;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Login extends AppCompatActivity {


    MyAnnotation myAnnotation = new MyAnnotation();


    private AppCompatEditText edPlace;
    private AppCompatEditText edPassword;

    @TextViewAnnotation(R.id.btnGo)
    public Button btnGo;

    private AlertDialog dialog;
    private Call call;
    private Tracker mTracker;
    private String imei_number;
    private static final int REQUEST_IMEI_PERMISSION = 1111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            myAnnotation.bind(this);


            mTracker = ((Flint) getApplication()).getDefaultTracker();

            //Blur background Image
            ImageView imageViewBg = (ImageView) findViewById(R.id.img_bg);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_bg2);
            bitmap = ImageUtils.getBlurredImage(bitmap, 0.2f, 10);
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            imageViewBg.setBackground(d);

            initViews();




            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SPUtils spUtils = new SPUtils(Login.this);
                    String token = spUtils.getToken();

                    if (token != null && token.length() > 0) {
                        //redirect to Home Screen
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    } else {
                        //show login form

                        findViewById(R.id.img_fleet_iq_icon).animate().setDuration(500).translationY(-400);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                        alphaAnimation.setDuration(1500);
                        alphaAnimation.setStartOffset(0);
                        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                findViewById(R.id.form_parent).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        findViewById(R.id.form_parent).startAnimation(alphaAnimation);
                    }

                }
            }, 2000);


            requestImei();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) call.cancel();
    }

    private void requestImei() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_IMEI_PERMISSION);
            } else {
                //Updating IMEI Number in shared preferences
                imei_number = GeneralUtilities.getDeviceImei(Login.this);

            }
        }catch (Exception e)
        {
            e.printStackTrace();
            ((Flint)getApplication()).trackException(e);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_IMEI_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Updating IMEI Number in shared preferences
            imei_number = GeneralUtilities.getDeviceImei(Login.this);
        }
    }

    private void initViews() {
        edPlace = (AppCompatEditText) findViewById(R.id.edPlace);
        edPassword = (AppCompatEditText) findViewById(R.id.edPassword);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });

        dialog = new SpotsDialog(this, R.style.Custom);
    }

    private boolean validateFormValues(String username, String password) {
        if (username.length() == 0) {
            edPlace.setError("Please enter username");
            return false;
        } else if (password.toString().length() == 0) {
            edPassword.setError("Please enter password");
            return false;
        }
        return true;
    }

    private void login() {



        final String username = edPlace.getText().toString();
        final String password = edPassword.getText().toString();

        //validate form values for incorract values
        if (!validateFormValues(username, password))
            return;


        dialog.show();
        String body = "username=" + username +
                "&password=" + password +
                "&applicationid=" + "9a959887-5946-11e6-9bb0-fe984cc15272";

        call = RESTClient.Login(this, body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dialog.dismiss();
                Snackbar snackbar = Snackbar.make(findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        login();
                    }
                });
                snackbar.show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {

                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        if (jsonObject.has("valid")) {
                            String valid = jsonObject.getString("valid");
                            if (valid.equalsIgnoreCase("true")) {

                                JSONObject jsonObject1 = jsonObject.getJSONObject("object");

                                String access_token = jsonObject1.getString("access_token");
                                String userKey = jsonObject1.getString("userKey");
                                String access_key = jsonObject1.getString("access_key");
                                String user_id = jsonObject1.getString("user_id");

                                SPUtils spUtils = new SPUtils(Login.this);
                                spUtils.setValue(SPUtils.ACCESS_TOKEN, access_token);
                                spUtils.setValue(SPUtils.ACCESS_KEY, access_key);
                                spUtils.setValue(SPUtils.USER_KEY, userKey);
                                spUtils.setValue(SPUtils.USER_ID, user_id);

                                if(mTracker!=null) {
                                    mTracker.setScreenName("User Login");
                                    mTracker.set("&cd1", imei_number);
                                    mTracker.set("&cd2", user_id);
                                    mTracker.set("&cd3", GeneralUtilities.getCurrentIST_Time());
                                    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                                }



                                startActivity(new Intent(Login.this, MainActivity.class));
                                finish();
                            }
                            else {
                                /*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Login.this, "Login unsuccessful. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });*/
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.root),"Login unsuccessful.Please try again.", Snackbar.LENGTH_LONG);
                                snackbar.setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        login();
                                    }
                                });
                                snackbar.show();
                            }
                        } else {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.root), getResources().getString(R.string.login_unsuccess), Snackbar.LENGTH_LONG);
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    login();
                                }
                            });
                            snackbar.show();
//                            Toast.makeText(Login.this,"Login ")

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                        snackbar.setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                login();
                            }
                        });
                        snackbar.show();
                    } finally {
                        dialog.dismiss();
                    }
                } else {
                    dialog.dismiss();
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                    snackbar.setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            login();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }
}
