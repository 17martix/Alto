package com.structurecode.alto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.structurecode.alto.Helpers.StorageHandler;

import java.util.ArrayList;

public class SplashScreenActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static int SPLASH_TIME_OUT = 3000;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }else {
            check_login();
        }

    }

    private void login(){
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreenActivity.this, AuthActivity.class);
            startActivity(i);
            finish();
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        if(Manifest.permission.READ_PHONE_STATE.equals(permission)) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                close_app();
                            }
                        }
                        if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                close_app();
                            }
                        }

                        check_login();
                    }
                } else {
                    close_app();
                }
                break;
            }
        }
    }

    private void close_app(){
        finish();
        System.exit(0);
    }

    private void check_login(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            new Handler().postDelayed(() -> {
                Intent i = new Intent(SplashScreenActivity.this, LibraryActivity.class);
                startActivity(i);
                finish();
            }, SPLASH_TIME_OUT);
        }else{
            login();
        }
    }
}
