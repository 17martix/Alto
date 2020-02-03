package com.structurecode.alto;

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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Services.PlayerService;

import static com.structurecode.alto.Helpers.Utils.DEVICE_CHECK;

public class PlayerActivity extends AppCompatActivity {
    private PlayerView playerView;
    private PlayerService player;
    private boolean serviceBound=false;
    private Intent serviceIntent=null;
    private TextView song_info;
    private BroadcastReceiver checkBroadcast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_media_player);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        playerView=findViewById(R.id.audio_view);
        song_info= findViewById(R.id.SongInfo);
        startService();
        playerView.showController();

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

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            playerView.setPlayer(player.player);
            song_info.setText(player.GetPlayingInfo());
            //song_info.setText(player.player.getCurrentTag().toString());

            player.player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == PlaybackStateCompat.STATE_PLAYING){
                        song_info.setText(player.GetPlayingInfo());
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            //player.stopSelf();
        }

        if (checkBroadcast != null) {
            unregisterReceiver(checkBroadcast);
            checkBroadcast = null;
        }
    }

    public void startService(){
        if(serviceIntent==null){
            serviceIntent = new Intent(this, PlayerService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

}
