package com.structurecode.alto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Query;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.structurecode.alto.Adapters.SongArtistAlbumAdapter;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.Services.PlayerService;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;

public class SearchActivity extends BaseActivity {
    private PlayerControlView playerControlView;
    public PlayerService player;
    private boolean serviceBound=false;
    private Intent serviceIntent=null;

    private EditText search_query;
    private RecyclerView recyclerView;
    private SongArtistAlbumAdapter adapter;
    private ArrayList<Song> list;
    private CoordinatorLayout coordinatorLayout;
    private LinearLayout mini_player_music;
    private TextView song_info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        search_query = findViewById(R.id.search_query);
        recyclerView = findViewById(R.id.search_result);
        mini_player_music = findViewById(R.id.mini_player_music);
        coordinatorLayout = findViewById(R.id.coord_music);
        playerControlView=findViewById(R.id.audio_view);
        song_info= findViewById(R.id.SongInfo);

        mAuth= FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        startService();

        db.collection(Utils.COLLECTION_SONGS).limit(50).orderBy("title", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                list = new ArrayList<>();
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    list.add(snapshot.toObject(Song.class));
                }

                adapter = new SongArtistAlbumAdapter(list,SearchActivity.this,true);
                recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                recyclerView.setAdapter(adapter);
            }
        });

        mini_player_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SearchActivity.this, PlayerActivity.class);
                startActivity(intent);
            }
        });

        search_query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Query query = new Query(s.toString())
                        .setHitsPerPage(50);
                player.getIndex().searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                        Log.e("HELLO", jsonObject.toString());
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            //player.stopSelf();
        }

        super.onDestroy();
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.navigation_search;
    }

    @Override
    Context getContext() {
        return SearchActivity.this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

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

            update_player();
            player.player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == PlaybackStateCompat.STATE_PLAYING){
                        song_info.setText(player.GetPlayingInfo());
                        Utils.show_mini_player(true,SearchActivity.this,coordinatorLayout,mini_player_music);
                    }

                    if (playbackState == PlaybackStateCompat.STATE_STOPPED){
                        Utils.show_mini_player(false,SearchActivity.this,coordinatorLayout,mini_player_music);
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
            Utils.show_mini_player(true,SearchActivity.this,coordinatorLayout,mini_player_music);
        }
    }
}
