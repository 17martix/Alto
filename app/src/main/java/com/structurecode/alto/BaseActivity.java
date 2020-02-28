package com.structurecode.alto;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Services.PlayerService;
import com.structurecode.alto.Services.SongDownloadService;

import static com.structurecode.alto.Helpers.Utils.DEVICE_CHECK;
import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;
    private Context context;
    private BroadcastReceiver checkBroadcast;

    private PlayerControlView playerControlView;
    private PlayerView playerView;
    public PlayerService player;
    private boolean serviceBound=false;
    private Intent serviceIntent=null;

    private LinearLayout mini_player_music;
    private CoordinatorLayout coordinatorLayout;
    private TextView song_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        context = getContext();

        mAuth=FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);

        mini_player_music = findViewById(R.id.mini_player_music);
        coordinatorLayout = findViewById(R.id.coord_music);
        playerControlView=findViewById(R.id.audio_view);
        playerView = findViewById(R.id.artwork);
        song_info= findViewById(R.id.SongInfo);

        playerView.hideController();
        startService();

        try {
            DownloadService.start(getContext(), SongDownloadService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(getContext(), SongDownloadService.class);
        }

        mini_player_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), PlayerActivity.class);
                startActivity(intent);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DEVICE_CHECK);
        checkBroadcast =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                DownloadService.sendRemoveAllDownloads(getContext(),SongDownloadService.class,false);
                mAuth.signOut();
                Intent i=new Intent(context, AuthActivity.class);
                player.stopSelf();
                startActivity(i);
                finish();
            }
        };
        registerReceiver(checkBroadcast,intentFilter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }

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
        switch (itemId){
            case R.id.navigation_library:
                startActivity(new Intent(context, LibraryActivity.class));
                break;
            case R.id.navigation_search:
                startActivity(new Intent(context, SearchActivity.class));
                break;
            case R.id.navigation_explore:
                startActivity(new Intent(context, ExploreActivity.class));
                break;
        }
        return false;
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

    abstract Context getContext();

    public void startService(){
        if(serviceIntent==null){
            serviceIntent = new Intent(this, PlayerService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            playerControlView.setPlayer(player.player);
            playerView.setPlayer(player.player);
            update_player();
            player.player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == PlaybackStateCompat.STATE_PLAYING){
                        song_info.setText(player.GetPlayingInfo());
                        Utils.show_mini_player(true,context,coordinatorLayout,mini_player_music);
                    }

                    if (playbackState == PlaybackStateCompat.STATE_STOPPED){
                        Utils.show_mini_player(false,context,coordinatorLayout,mini_player_music);
                    }
                }

                @Override
                public void onPositionDiscontinuity(int reason) {
                    song_info.setText(player.GetPlayingInfo());
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public  void update_player(){
        if (player.player.getPlaybackState()!=Player.STATE_IDLE){
            song_info.setText(player.GetPlayingInfo());
            Utils.show_mini_player(true,context,coordinatorLayout,mini_player_music);
        }
    }

}
