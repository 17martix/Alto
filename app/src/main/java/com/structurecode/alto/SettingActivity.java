package com.structurecode.alto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.structurecode.alto.Fragments.SettingFragment;
import com.structurecode.alto.Helpers.Utils;

import static com.structurecode.alto.Helpers.Utils.DEVICE_CHECK;

public class SettingActivity extends AppCompatActivity {
    private BroadcastReceiver checkBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingFragment())
                .commit();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DEVICE_CHECK);
        checkBroadcast =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Intent i=new Intent(context, AuthActivity.class);
                startActivity(i);
                finish();
            }
        };
        registerReceiver(checkBroadcast,intentFilter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (checkBroadcast != null) {
            unregisterReceiver(checkBroadcast);
            checkBroadcast = null;
        }

        super.onDestroy();
    }
}
