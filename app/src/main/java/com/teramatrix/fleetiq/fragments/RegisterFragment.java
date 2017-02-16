package com.teramatrix.fleetiq.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teramatrix.fleetiq.Flint;
import com.teramatrix.fleetiq.R;
import com.teramatrix.fleetiq.Utils.GeneralUtilities;
import com.teramatrix.fleetiq.Utils.SPUtils;
import com.teramatrix.fleetiq.controller.RESTClient;
import com.teramatrix.fleetiq.model.OBDVehicelInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by arun.singh on 6/6/2016.
 */
public class RegisterFragment extends Fragment {


    private View convertView;
    private AlertDialog dialog;
    private Call call;
    private OBDVehicelInfo selectedOBDDevice;
    final ArrayList<OBDVehicelInfo> obdVehicelInfos = new ArrayList<OBDVehicelInfo>();
    String selected_idn;
    Snackbar snackbar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.register_vehicle_fragment, null, false);
            initViews();
            getNewOBDDevicesList();
        }
        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(snackbar!=null && snackbar.isShown())
            snackbar.dismiss();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        try {

            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.action_graph).setVisible(false);
            super.onPrepareOptionsMenu(menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initViews() {
        GeneralUtilities.setUpActionBar(getActivity(), this, "FLINT", null);
        dialog = new SpotsDialog(getActivity(), R.style.Custom);

        //Listener on IDN field
        convertView.findViewById(R.id.card_view_spinner_list).setTag("gone");
        convertView.findViewById(R.id.txt_idn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (obdVehicelInfos != null && obdVehicelInfos.size() > 0)
                    toggelIDNDropDown();
            }
        });

        convertView.findViewById(R.id.shadow_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggelIDNDropDown();
            }
        });


        //Done button
        convertView.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                convertView.findViewById(R.id.txt_invalid_idn).setVisibility(View.GONE);
                convertView.findViewById(R.id.txt_invalid_drivar_name).setVisibility(View.GONE);
                convertView.findViewById(R.id.txt_invalid_registration_number).setVisibility(View.GONE);
                convertView.findViewById(R.id.txt_invalid_model_name).setVisibility(View.GONE);

                if (selected_idn == null) {
                    //IDN not selected
                    convertView.findViewById(R.id.txt_invalid_idn).setVisibility(View.VISIBLE);
                    return;
                } else if (((EditText) convertView.findViewById(R.id.txt_driver_name)).getText().toString().length() == 0) {
                    //Drivaer name not entered
                    convertView.findViewById(R.id.txt_invalid_drivar_name).setVisibility(View.VISIBLE);
                    return;
                } else if (((EditText) convertView.findViewById(R.id.txt_registration_nubmer)).getText().toString().length() == 0) {
                    //Registration number not entered
                    convertView.findViewById(R.id.txt_invalid_registration_number).setVisibility(View.VISIBLE);
                    return;
                } else if (((EditText) convertView.findViewById(R.id.txt_model_code)).getText().toString().length() == 0) {
                    //Vehicle Model not entered
                    convertView.findViewById(R.id.txt_invalid_model_name).setVisibility(View.VISIBLE);
                    return;
                }


                selectedOBDDevice.driver_name = ((EditText) convertView.findViewById(R.id.txt_driver_name)).getText().toString();
                selectedOBDDevice.registration_no = ((EditText) convertView.findViewById(R.id.txt_registration_nubmer)).getText().toString();
                selectedOBDDevice.model_code = ((EditText) convertView.findViewById(R.id.txt_model_code)).getText().toString();
                //Register Selected Device with Form Data
                registerObdDevice();

            }
        });

    }

    private void populateDeviceIDNList() {

        final ArrayList<OBDVehicelInfo> ToBeAdded_obdVehicelInfosList = new ArrayList<OBDVehicelInfo>();
        if(HomeFragment.obdVehicelInfoArrayList.size()>0 && obdVehicelInfos.size() > 0)
        {
            for(OBDVehicelInfo AlreadyAdded_obdVehicelInfo: HomeFragment.obdVehicelInfoArrayList)
            {
                for(int i =0;i<obdVehicelInfos.size();i++)
                {
                    OBDVehicelInfo New_obdVehicelInfo = obdVehicelInfos.get(i);
                    if(AlreadyAdded_obdVehicelInfo.device_id.equalsIgnoreCase(New_obdVehicelInfo.device_id))
                    {
                        obdVehicelInfos.remove(i);
                        break;
                    }
                }
            }
        }else
        {
            return;
        }

        ToBeAdded_obdVehicelInfosList.addAll(obdVehicelInfos);

        if (ToBeAdded_obdVehicelInfosList != null && ToBeAdded_obdVehicelInfosList.size() > 0) {
            convertView.findViewById(R.id.txt_no_device_found).setVisibility(View.GONE);
            convertView.findViewById(R.id.registration_form).setVisibility(View.VISIBLE);
        } else
            return;


        //Set Dropdown Height according to number of items
        View card_view_spinner_list = convertView.findViewById(R.id.card_view_spinner_list);
        ViewGroup.LayoutParams layoutParams = card_view_spinner_list.getLayoutParams();
        int h = ToBeAdded_obdVehicelInfosList.size() * 100;
        layoutParams.height = h;
        card_view_spinner_list.setLayoutParams(layoutParams);


        //init list
        ListView list_view_idns = (ListView) convertView.findViewById(R.id.list_view_idns);
        list_view_idns.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return obdVehicelInfos.size();
            }

            @Override
            public Object getItem(int position) {
                return obdVehicelInfos.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView_local, ViewGroup parent) {

                ViewHolder viewHolder = null;
                if (convertView_local == null) {
                    viewHolder = new ViewHolder();
                    convertView_local = LayoutInflater.from(getActivity()).inflate(R.layout.layout_idn_row, null);
                    viewHolder.textViewIdn = (TextView) convertView_local.findViewById(R.id.idn_name);
                    viewHolder.rowView = convertView_local;
                    convertView_local.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView_local.getTag();
                }
                viewHolder.textViewIdn.setText(ToBeAdded_obdVehicelInfosList.get(position).device_name_alias);

                viewHolder.rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        selected_idn = ToBeAdded_obdVehicelInfosList.get(position).device_name_alias;
                        selectedOBDDevice = ToBeAdded_obdVehicelInfosList.get(position);
                        ((EditText) convertView.findViewById(R.id.txt_idn1)).setText(selected_idn);
                        toggelIDNDropDown();
                    }
                });

                return convertView_local;
            }

            class ViewHolder {
                TextView textViewIdn;
                View rowView;
            }
        });
    }

    private void toggelIDNDropDown() {
        if (convertView.findViewById(R.id.card_view_spinner_list).getTag().toString().equalsIgnoreCase("gone")) {
            convertView.findViewById(R.id.card_view_spinner_list).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.card_view_spinner_list).setTag("visible");
            convertView.findViewById(R.id.shadow_layout).setVisibility(View.VISIBLE);

        } else if (convertView.findViewById(R.id.card_view_spinner_list).getTag().toString().equalsIgnoreCase("visible")) {
            convertView.findViewById(R.id.card_view_spinner_list).setVisibility(View.GONE);
            convertView.findViewById(R.id.card_view_spinner_list).setTag("gone");
            convertView.findViewById(R.id.shadow_layout).setVisibility(View.GONE);
        }
    }

    private void getNewOBDDevicesList() {
        dialog.show();

        SPUtils spUtils = new SPUtils(getActivity());
        String body = "token=" + spUtils.getToken() +
                "&access_key=" + spUtils.getAccessKey() +
                "&userkey=" + spUtils.getUserKey() +
                "&user_id=" + spUtils.getUserID();

        call = RESTClient.getNewOBDDevices(getActivity(), body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dialog.dismiss();
                snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getNewOBDDevicesList();
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

                            if (jsonObject.getString("valid").equalsIgnoreCase("true")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("object");
                                if (jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                        OBDVehicelInfo obdVehicelInfo = new OBDVehicelInfo();

                                        if (jsonObject1.getString("device_device_device_id").contains(".")) {

                                            String s = jsonObject1.getString("device_device_device_id");
                                            int k  = (int)Float.parseFloat(s);
                                            obdVehicelInfo.device_id = k+"";
                                        }
                                        else {
                                            obdVehicelInfo.device_id = jsonObject1.getString("device_device_device_id");
                                        }
                                        obdVehicelInfo.device_name = jsonObject1.getString("device_device_name");
                                        obdVehicelInfo.device_name_alias = jsonObject1.getString("device_device_alias");
                                        obdVehicelInfos.add(obdVehicelInfo);
                                    }
                                }
                            }
                        } else {
                            snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.login_unsuccess), Snackbar.LENGTH_LONG);
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getNewOBDDevicesList();
                                }
                            });
                            snackbar.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                        snackbar.setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                login();
                                getNewOBDDevicesList();
                            }
                        });
                        snackbar.show();
                    } finally {
                        dialog.dismiss();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateDeviceIDNList();
                            }
                        });

                    }
                } else {
                    dialog.dismiss();
                    snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                    snackbar.setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            login();
                            getNewOBDDevicesList();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

    String response_messag ="";
    private void registerObdDevice() {
        if (selectedOBDDevice == null)
            return;
        dialog.show();
        SPUtils spUtils = new SPUtils(getActivity());
        String body = "token=" + spUtils.getToken() +
                "&userkey=" + spUtils.getUserKey() +
                "&user_id=" + spUtils.getUserID() +
                "&device_id=" + selectedOBDDevice.device_id +
                "&device_name=" + selectedOBDDevice.device_name +
                "&vehicle_identity_no=" + "123456789" +
                "&registration_no=" + selectedOBDDevice.registration_no +
                "&engine_number=" + "123456789" +
                "&chassis_number=" + "123456789" +
                "&model_code=" + selectedOBDDevice.model_code +
                "&condition=" + "Brand new" +
                "&fuel_type=" + "Petrol" +
                "&vehicle_type=" + "car" +
                "&vechicle_status=" + "Good" +
                "&color_code=" + "#ffffff" +
                "&purchase_date=" + "2016-06-24 00:12:02" +
                "&is_active=" + "1" +
                "&driver_name=" + selectedOBDDevice.driver_name;

        call = RESTClient.DeviceRegistration(getActivity(), body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    dialog.dismiss();
                    snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            registerObdDevice();
                        }
                    });
                    snackbar.show();
                }catch (Exception ee)
                {
                    ee.printStackTrace();
                    ((Flint) getActivity().getApplication()).trackException(e);
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();

                        System.out.print("response_messag:"+res);
                        JSONObject jsonObject = new JSONObject(res);
                        if (jsonObject.has("valid")) {

                            if (jsonObject.getString("valid").equalsIgnoreCase("true")) {

                            }
                            if(jsonObject.has("object"))
                            {
                                JSONArray jsonArray = jsonObject.getJSONArray("object");
                                if(jsonArray!=null && jsonArray.length()>0)
                                {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                                    if(jsonObject1.has("message"))
                                    {
                                        response_messag = jsonObject1.getString("message");
                                    }
                                }
                            }
                        } else {
                            snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.login_unsuccess), Snackbar.LENGTH_LONG);
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    registerObdDevice();
                                }
                            });
                            snackbar.show();
                        }
                    } catch (Exception e) {
                        try {
                            e.printStackTrace();
                            snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                login();
                                    registerObdDevice();
                                }
                            });
                            snackbar.show();
                        }catch (Exception ee)
                        {
                            ee.printStackTrace();
                            ((Flint) getActivity().getApplication()).trackException(e);
                        }
                    } finally {
                        dialog.dismiss();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                Toast.makeText(getActivity(),response_messag+"",Toast.LENGTH_SHORT).show();
                                getActivity().sendBroadcast(new Intent("ACTION_GET_ALL_OBD_SOURCE"));
                            }
                        });
                    }
                } else {
                    try {
                        dialog.dismiss();
                        snackbar = Snackbar.make(convertView.findViewById(R.id.root), getResources().getString(R.string.request_failure), Snackbar.LENGTH_LONG);
                        snackbar.setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                registerObdDevice();
                            }
                        });
                        snackbar.show();
                    }catch (Exception ee)
                    {
                        ee.printStackTrace();
                        ((Flint) getActivity().getApplication()).trackException(ee);
                    }
                }
            }
        });
    }
}
