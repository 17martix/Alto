package com.structurecode.alto;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.ListPreference;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.structurecode.alto.Fragments.SongFragment;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.Services.PlayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.structurecode.alto.Helpers.Utils.user;
import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;

public class LibraryActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private PlayerControlView playerControlView;
    public PlayerService player;
    private boolean serviceBound=false;
    private Intent serviceIntent=null;

    private LinearLayout mini_player_music;
    private CoordinatorLayout coordinatorLayout;
    private TextView song_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_music);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.pager_music);
        mini_player_music = findViewById(R.id.mini_player_music);
        coordinatorLayout = findViewById(R.id.coord_music);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs_music);
        tabLayout.setupWithViewPager(viewPager);
        song_info= findViewById(R.id.SongInfo);

        mAuth=FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        playerControlView=findViewById(R.id.audio_view);

        startService();

        mini_player_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), PlayerActivity.class);
                startActivity(intent);
            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SongFragment(), getString(R.string.songs));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent intent=new Intent(LibraryActivity.this,SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.action_add_playlist:
                display_new_playlist_dialog(R.string.new_playlist,R.string.create,"",false,0);
                break;
            case R.id.action_shuffle:
                CollectionReference col = db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                        .collection(Utils.COLLECTION_LIBRARY);
                col.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    /*List<Song> list = queryDocumentSnapshots.toObjects(Song.class);
                    int random = new Random().nextInt(list.size());
                    Song song = list.get(random);
                    player.play_song(song,list);*/
                    SongFragment fragment = ((SongFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_library_songs));
                    int total_Count = fragment.getRecyclerView().getAdapter().getItemCount()-1;
                    int random = new Random().nextInt(total_Count);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragment.getRecyclerView().findViewHolderForAdapterPosition(random).itemView.performClick();
                        }
                    },1);
                });
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_library;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.navigation_library;
    }

    private void display_new_playlist_dialog(@StringRes int title, @StringRes int positiveText, final String playlistTitle,
                                             boolean is_private_retrieved, final int playlistId){

        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View view = li.inflate(R.layout.dialog_playlist, null);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String exposure=getString(R.string.exposure_public);
                        boolean is_private = ((CheckBox) view.findViewById(R.id.exposure)).isChecked();
                        if(is_private) exposure=getString(R.string.exposure_private);

                        String inputTitle = ((EditText) view.findViewById(R.id.title_txt)).getText().toString();
                        if (inputTitle==null || inputTitle.isEmpty()) {
                            inputTitle = getString(R.string.untitled);
                        }else {
                            inputTitle=inputTitle.trim();
                        }

                        // CODE FFFFFFFFFFFFFFFFFFFFF
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    public void startService(){
        if(serviceIntent==null){
            serviceIntent = new Intent(this, PlayerService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            playerControlView.setPlayer(player.player);
            update_player();
            player.player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == PlaybackStateCompat.STATE_PLAYING){
                        song_info.setText(player.GetPlayingInfo());
                        Utils.show_mini_player(true,LibraryActivity.this,coordinatorLayout,mini_player_music);
                    }

                    if (playbackState == PlaybackStateCompat.STATE_STOPPED){
                        Utils.show_mini_player(false,LibraryActivity.this,coordinatorLayout,mini_player_music);
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
            Utils.show_mini_player(true,LibraryActivity.this,coordinatorLayout,mini_player_music);
        }
    }
}
