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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.google.android.material.snackbar.Snackbar;
import com.structurecode.alto.Helpers.NetworkChecker;
import com.structurecode.alto.Helpers.Verification;
import com.structurecode.alto.IdentificationActivty;
import com.structurecode.alto.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;
import static com.structurecode.alto.Helpers.Utils.registration_success_broadcast;

/**
 * Created by Guy on 6/23/2017.
 */

public class RegistrationFragment extends Fragment {
    private Button sign_up;
    private EditText name;
    private EditText e_mail;
    private EditText code;
    private EditText code_c;
    private RelativeLayout fragment;
    private ProgressDialog progressDialog;

    private boolean valid_mail = false;
    private boolean valid_code = false;
    private boolean code_match = false;
    private boolean valid_name = false;
    private boolean valid_username = false;
    private boolean registration_ready=false;

    public RegistrationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_registration, container, false);

        e_mail = (EditText)view.findViewById(R.id.mail);
        code = (EditText)view.findViewById(R.id.password);
        code_c = (EditText) view.findViewById(R.id.c_password);
        sign_up = (Button) view.findViewById(R.id.sign_2);
        name = (EditText)view.findViewById(R.id.name);
        fragment=(RelativeLayout)view.findViewById(R.id.fragment_registration);

        name.setOnFocusChangeListener(name_watcher);
        e_mail.setOnFocusChangeListener(email_watcher);
        code.setOnFocusChangeListener(code_watcher);
        code_c.setOnFocusChangeListener(code_c_watcher);
        sign_up.setOnClickListener(sign_onClickListener);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected View.OnClickListener sign_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            name.clearFocus();
            e_mail.clearFocus();
            code.clearFocus();
            code_c.clearFocus();
            if (!valid_name) name.requestFocus();
            else if (!valid_mail) e_mail.requestFocus();
            else if (!valid_code) code.requestFocus();
            else if (!code_match) code_c.requestFocus();
            else{
                if (NetworkChecker.isConnected(getActivity())){
                    registration_ready=true;
                    registerUser();

                }else {
                    Toast.makeText(getActivity(),R.string.internet_fail, Toast.LENGTH_LONG).show();
                }
            }
        }

    };

    private View.OnFocusChangeListener name_watcher=new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                name.setError(null);
                String result= Verification.name_check(name.getText().toString(),getActivity());
                if (result.equals("")){
                    valid_name=true;
                }else {
                    name.setError(result);
                    valid_name=false;
                }
            }
        }
    };

    private View.OnFocusChangeListener email_watcher=new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                e_mail.setError(null);
                String result= Verification.email_check(e_mail.getText().toString(),getActivity(),true);
                if (result.equals("")){
                    valid_mail=true;
                }else {
                    e_mail.setError(result);
                    valid_mail=false;
                }
            }
        }
    };
    private View.OnFocusChangeListener code_watcher=new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                code.setError(null);
                String result= Verification.password_check(code.getText().toString(),getActivity());
                if (result.equals("")){
                    valid_code=true;
                }else {
                    code.setError(result);
                    valid_code=false;
                }
            }
        }
    };
    private View.OnFocusChangeListener code_c_watcher=new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                code_c.setError(null);
                String result= Verification.cPassword_check(code.getText().toString(),code_c.getText().toString(),
                        getActivity());
                if (result.equals("")){
                    code_match=true;
                }else {
                    code_c.setError(result);
                    code_match=false;
                }
            }
        }
    };

    private void registerUser(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.authenticate));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        String username=name.getText().toString();
        String email=e_mail.getText().toString();
        String password=code.getText().toString();

        name.setError(null);
        e_mail.setError(null);
        code.setError(null);
        code_c.setError(null);

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("name", username);
        AWSMobileClient.getInstance().signUp(email, password, attributes, null, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.d("SIGNED UP", "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            Toast.makeText(getContext(),"Confirm sign-up with: " + details.getDestination(),Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(),"Sign-up done.",Toast.LENGTH_LONG).show();
                            name.setText(null);
                            code.setText(null);
                            code_c.setText(null);
                            e_mail.setText(null);

                            name.setError(null);
                            e_mail.setError(null);
                            code.setError(null);
                            code_c.setError(null);

                            valid_mail = false;
                            valid_code = false;
                            code_match = false;
                            valid_name = false;
                            registration_ready=false;

                            Intent i=new Intent();
                            i.setAction(registration_success_broadcast);
                            ((IdentificationActivty)getActivity()).selectFragment(0);
                            getActivity().sendBroadcast(i);
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("SIGNED UP", "Sign-up error", e);
                Snackbar.make(fragment,R.string.registration_error,Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        registerUser();
                    }
                }).show();
            }
        });
    }
}
