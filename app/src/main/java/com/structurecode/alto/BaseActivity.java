package com.structurecode.alto;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.structurecode.alto.Helpers.Utils;

import static com.structurecode.alto.Helpers.Utils.DEVICE_CHECK;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;
    private BroadcastReceiver checkBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);

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
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onDestroy() {
        if (checkBroadcast != null) {
            unregisterReceiver(checkBroadcast);
            checkBroadcast = null;
        }

        super.onDestroy();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_library) {
            startActivity(new Intent(this, LibraryActivity.class));
        }
        finish();
        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getBottomNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        item.setChecked(true);
    }

    abstract int getLayoutId(); // this is to return which layout(activity) needs to display when clicked on tabs.

    abstract int getBottomNavigationMenuItemId();//Which menu item selected and change the state of that menu item
}
