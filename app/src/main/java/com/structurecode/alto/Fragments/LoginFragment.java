package com.structurecode.alto.Fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by Guy on 6/23/2017.
 */

public class LoginFragment  extends Fragment {
    private EditText UsernameInput;
    private EditText PasswordInput;
    private Button SignInButon;
    private String LoginUrl;
    private LinearLayout fragment;

    private ProgressDialog progressDialog;
    private DatabaseHandler database;
    private BroadcastReceiver registration_receiver;
    private BroadcastReceiver urlReceiver;
    private BroadcastReceiver loginReceiver;

    private String username;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_login, container, false);

        LoginUrl=getString(R.string.login_url);
        UsernameInput =(EditText)view.findViewById(R.id.login_username);
        PasswordInput=(EditText)view.findViewById(R.id.login_password);
        SignInButon=(Button)view.findViewById(R.id.sign_in_button);
        fragment=(LinearLayout) view.findViewById(R.id.fragment_login);
        database=new DatabaseHandler(getActivity());

        IntentFilter loginFilter = new IntentFilter();
        loginFilter.addAction(Utilities.loginBroadcast);
        loginReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UsernameInput.setError(getString(R.string.login_error));
                PasswordInput.setError(getString(R.string.login_error));
                UsernameInput.requestFocus();
            }
        };

        IntentFilter urlFilter = new IntentFilter();
        urlFilter.addAction(Utilities.urlBroadcast);
        urlReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getActivity(),R.string.server_fail,Toast.LENGTH_LONG).show();
            }
        };

        IntentFilter registrationFilter = new IntentFilter();
        registrationFilter.addAction(Utilities.registration_success_broadcast);
        registration_receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Snackbar.make(fragment,R.string.email_sent_registration, Snackbar.LENGTH_INDEFINITE).show();
            }
        };

        getActivity().registerReceiver(loginReceiver,loginFilter);
        getActivity().registerReceiver(urlReceiver,urlFilter);
        getActivity().registerReceiver(registration_receiver,registrationFilter);

        SignInButon.setOnClickListener(login_onClickListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(registration_receiver);
        getActivity().unregisterReceiver(urlReceiver);
        getActivity().unregisterReceiver(loginReceiver);
        super.onDestroyView();
    }

    private View.OnClickListener login_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (NetworkChecker.isConnected(getActivity())) {
                username=UsernameInput.getText().toString();
                String password=PasswordInput.getText().toString();
                String devices=Utilities.device_name_id(getContext());

                if (username==null||username.isEmpty() ){
                    UsernameInput.setError(getString(R.string.uname_empty));
                    UsernameInput.requestFocus();
                }else if (password==null||password.isEmpty()){
                    PasswordInput.setError(getString(R.string.password_empty));
                    PasswordInput.requestFocus();
                }else{
                    UsernameInput.setError(null);
                    PasswordInput.setError(null);

                    JSONObject user=new JSONObject();
                    try{
                        user.put("username" , username);
                        user.put("password", password);
                        user.put("devices", devices);
                    }catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(getActivity(),R.string.random_error,Toast.LENGTH_LONG).show();
                    }
                    if (user.length()>0){
                        ServerRequest serverRequest=new ServerRequest();
                        serverRequest.execute(String.valueOf(user));

                    }
                }
            } else {
                Toast.makeText(getActivity(), R.string.internet_fail, Toast.LENGTH_LONG).show();
            }
        }
    };

    public class ServerRequest extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.authenticating));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String response= Utilities.POSTRequest(LoginUrl,params[0]);
                return response;
            } catch (MalformedURLException e) {
                //e.printStackTrace();
                progressDialog.dismiss();
                Intent intent=new Intent();
                intent.setAction(Utilities.urlBroadcast);
                getActivity().sendBroadcast(intent);
                return null;
            } catch (IOException e) {
                //e.printStackTrace();
                progressDialog.dismiss();
                Intent intent=new Intent();
                intent.setAction(Utilities.loginBroadcast);
                getActivity().sendBroadcast(intent);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if(response==null || response.isEmpty()){
                Intent intent=new Intent();
                Log.e("FFFFFF1","HELLLOOOOOO");
                intent.setAction(Utilities.loginBroadcast);
                getActivity().sendBroadcast(intent);
            }else {
                try {
                    JSONObject object=new JSONObject(response);
                    String token=object.getString("token");
                    String device_response=object.getString("device_response");
                    String user_id=object.getString("user_id");
                    String username=object.getString("username");
                    String user_email=object.getString("user_email");
                    String country=object.getString("country");
                    String language=object.getString("language");
                    String license_id=object.getString("license_id");
                    String license_name=object.getString("license_name");
                    String setting_id=object.getString("setting_id");
                    String is_library_downloaded=object.getString("is_library_downloaded");
                    String is_playlist_downloaded=object.getString("is_playlist_downloaded");
                    String cache_size=object.getString("cache_size");
                    String refresh_rate=object.getString("refresh_rate");
                    String setting_created_at=object.getString("setting_created_at");
                    String setting_updated_at=object.getString("setting_updated_at");
                    if (device_response.equals("ok")){
                        database.insert_account(1,token,Integer.parseInt(user_id),username,user_email
                                ,country,language,Integer.parseInt(license_id),license_name,false);
                        database.insert_update_settings(Integer.parseInt(setting_id),Integer.parseInt(is_library_downloaded),
                                Integer.parseInt(is_playlist_downloaded),Integer.parseInt(cache_size),Integer.parseInt(refresh_rate),
                                setting_created_at,setting_updated_at,false);
                        Intent i=new Intent(getActivity(),MusicActivity.class);
                        startActivity(i);
                    }else  if (device_response.equals("device_saturated" )){
                        Intent i = new Intent(getActivity(), ResetDevicesActivity.class);
                        i.putExtra("token",token);
                        startActivity(i);
                    }else if (device_response.equals("device_not_found" )){
                        Intent intent=new Intent();
                        intent.setAction(Utilities.loginBroadcast);
                        getActivity().sendBroadcast(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Intent intent=new Intent();
                    Log.e("FFFFFF2","HELLLOOOOOO "+e.getMessage());
                    intent.setAction(Utilities.loginBroadcast);
                    getActivity().sendBroadcast(intent);
                }
            }
        }
    }
}
