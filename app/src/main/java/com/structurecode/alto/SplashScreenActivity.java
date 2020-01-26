package com.structurecode.alto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.structurecode.alto.Helpers.StorageHandler;

public class SplashScreenActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        StorageHandler.initDirs(SplashScreenActivity.this);
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

    private void login(){
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreenActivity.this, AuthActivity.class);
            startActivity(i);
            finish();
        }, SPLASH_TIME_OUT);
    }
}
