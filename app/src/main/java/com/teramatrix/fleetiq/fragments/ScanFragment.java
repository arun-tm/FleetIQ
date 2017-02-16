package com.teramatrix.fleetiq.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.SPUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class ScanFragment extends Fragment {


    private View convertView;
    ProgressDialog progress;
    private int progress_value = 0;
    private Timer timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.scan_fragment, null, false);
        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        try {

            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.action_graph).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(timer!=null)
            timer.cancel();
    }

    private void initViews() {
        try {
            GeneralUtilities.setUpActionBar(getActivity(), this, "Scan Vehicle", null);

            String is_scan_result_shown = new SPUtils(getActivity()).getString(SPUtils.IS_SCAN_RESULT_SHOWN);
            if (is_scan_result_shown == null || is_scan_result_shown.length() == 0 || is_scan_result_shown.equalsIgnoreCase("no")) {
                convertView.findViewById(R.id.error_details_parent).setVisibility(View.GONE);
                convertView.findViewById(R.id.clear_msg).setVisibility(View.VISIBLE);
            } else if (is_scan_result_shown.equalsIgnoreCase("yes")) {
                convertView.findViewById(R.id.error_details_parent).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.clear_msg).setVisibility(View.GONE);
            }
            convertView.findViewById(R.id.card_view_clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SPUtils(getActivity()).setValue(SPUtils.IS_SCAN_RESULT_SHOWN, "no");
                    convertView.findViewById(R.id.error_details_parent).setVisibility(View.GONE);
                    convertView.findViewById(R.id.clear_msg).setVisibility(View.VISIBLE);
                }
            });
            convertView.findViewById(R.id.card_view_scan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new SPUtils(getActivity()).setValue(SPUtils.IS_SCAN_RESULT_SHOWN, "yes");

                    progress_value = 0;
                    progress = new ProgressDialog(getActivity());
                    progress.setMessage("Scanning ...");
                    progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progress.setCancelable(false);
                    progress.show();

                    timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {

                            try {
                                System.out.print("Scan progress:" + progress_value);
                                if (progress_value >= 100) {
                                    progress.dismiss();
                                    timer.cancel();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            convertView.findViewById(R.id.error_details_parent).setVisibility(View.VISIBLE);
                                            convertView.findViewById(R.id.clear_msg).setVisibility(View.GONE);
                                        }
                                    });

                                } else {
                                    progress.setProgress(progress_value);
                                    progress_value++;
                                }
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                                ((Flint) getActivity().getApplication()).trackException(e);
                            }
                        }
                    };

                    timer.schedule(timerTask, 0, 500);

                }
            });
        }catch ( Exception ee)
        {
            ee.printStackTrace();
            ((Flint) getActivity().getApplication()).trackException(ee);
        }
    }
}
