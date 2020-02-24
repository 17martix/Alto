package com.structurecode.alto.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.structurecode.alto.Adapters.SongAdapter;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.user;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_COMPLETED;
import static com.structurecode.alto.Services.PlayerService.setting;

public class TrendingFragment extends Fragment {

    private SongAdapter adapter;
    private RecyclerView recyclerView;

    private BroadcastReceiver download_completed_broadcast;

    public TrendingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_trending, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.trending_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query query = db.collection(Utils.COLLECTION_METRICS).whereArrayContains("license",setting.getLicense())
                .orderBy("daily_play",Query.Direction.DESCENDING)
                .limit(50);

        FirestoreRecyclerOptions<Song> options = new FirestoreRecyclerOptions.Builder<Song>()
                .setQuery(query, Song.class)
                .build();
        adapter = new SongAdapter(options,getContext(),true,false);

        recyclerView.setAdapter(adapter);

        initialize_broadcasts();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        //adapter.stopListening();
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
    public void onDestroy() {
        if (download_completed_broadcast != null) {
            getContext().unregisterReceiver(download_completed_broadcast);
            download_completed_broadcast = null;
        }
        super.onDestroy();
    }
}