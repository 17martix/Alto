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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.CompletionHandler;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.structurecode.alto.Adapters.OnlineSongAdapter;
import com.structurecode.alto.Adapters.SongArtistAlbumAdapter;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.Services.PlayerService;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_COMPLETED;

public class SearchActivity extends BaseActivity {

    private EditText search_query;
    private RecyclerView recyclerView;
    private OnlineSongAdapter adapter;
    private ArrayList<Song> list;
    private BroadcastReceiver download_completed_broadcast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        search_query = findViewById(R.id.search_query);
        recyclerView = findViewById(R.id.search_result);

        mAuth= FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        startService();
        initialize_broadcasts();

        /*db.collection(Utils.COLLECTION_METRICS).limit(50).orderBy("monthly_play", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                list = new ArrayList<>();
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    list.add(new Song(snapshot.getId(),snapshot.getString("title"),
                            snapshot.getString("artist"),snapshot.getString("album"),
                            snapshot.getString("path"),snapshot.getString("url"),snapshot.getString("lyrics")));
                }

                adapter = new OnlineSongAdapter(list,SearchActivity.this,true);
                recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                recyclerView.setAdapter(adapter);
            }
        });*/

        db.collection(Utils.COLLECTION_METRICS).limit(50).orderBy("monthly_play", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                list = new ArrayList<>();
                if (task.isSuccessful()){
                    if (task.getResult()== null || task.getResult().isEmpty()){
                        placeholder();
                    }else {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            list.add(snapshot.toObject(Song.class));
                        }

                        adapter = new OnlineSongAdapter(list,SearchActivity.this,true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                        recyclerView.setAdapter(adapter);
                    }
                }else {
                    placeholder();
                }
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
                /*Query query = new Query(s.toString())
                        .setHitsPerPage(50);
                player.getIndex().searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                        Log.e("HELLO", jsonObject.toString());
                    }
                });*/
            }
        });

    }

    public void placeholder(){
        db.collection(Utils.COLLECTION_SONGS).limit(150).orderBy("year", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    /*list.add(new Song(snapshot.getId(),snapshot.getString("title"),
                            snapshot.getString("artist"),snapshot.getString("album"),
                            snapshot.getString("path"),snapshot.getString("url"),snapshot.getString("lyrics")));*/
                    list.add(snapshot.toObject(Song.class));
                }

                adapter = new OnlineSongAdapter(list,SearchActivity.this,true);
                recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                recyclerView.setAdapter(adapter);
            }
        });
    }

    public void initialize_broadcasts(){

        IntentFilter download_completed_filter = new IntentFilter();
        download_completed_filter.addAction(DOWNLOAD_COMPLETED);
        download_completed_broadcast =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };
        getContext().registerReceiver(download_completed_broadcast,download_completed_filter);
    }

    @Override
    protected void onDestroy() {
        if (download_completed_broadcast != null) {
            getContext().unregisterReceiver(download_completed_broadcast);
            download_completed_broadcast = null;
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
}
