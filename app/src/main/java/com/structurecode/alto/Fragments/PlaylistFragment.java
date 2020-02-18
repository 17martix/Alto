package com.structurecode.alto.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.structurecode.alto.Adapters.PlaylistAdapter;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.LibraryActivity;
import com.structurecode.alto.Models.Playlist;
import com.structurecode.alto.R;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.user;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_COMPLETED;

/**
 * Created by Guy on 4/23/2017.
 */

public class PlaylistFragment extends Fragment {
    private PlaylistAdapter adapter;
    private RecyclerView recyclerView;
    private BroadcastReceiver added_to_playlist_broadcast;
    private BroadcastReceiver remove_to_playlist_broadcast;
    private BroadcastReceiver download_completed_broadcast;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_library_playlists, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.PlaylistList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query query = db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                .collection(Utils.COLLECTION_PLAYLISTS).orderBy("title",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Playlist> options = new FirestoreRecyclerOptions.Builder<Playlist>()
                .setQuery(query, Playlist.class)
                .build();
        adapter = new PlaylistAdapter(options,getContext());
        recyclerView.setAdapter(adapter);

        initialize_broadcasts();

        return view;
    }

    public void initialize_broadcasts(){
        IntentFilter added_to_playlist_filter = new IntentFilter();
        added_to_playlist_filter.addAction(Utils.ADDED_TO_PLAYLIST);
        added_to_playlist_broadcast =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter remove_to_playlist_filter = new IntentFilter();
        remove_to_playlist_filter.addAction(Utils.REMOVED_TO_PLAYLIST);
        remove_to_playlist_broadcast =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter download_completed_filter = new IntentFilter();
        download_completed_filter.addAction(DOWNLOAD_COMPLETED);
        download_completed_broadcast =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };

        getContext().registerReceiver(download_completed_broadcast,download_completed_filter);
        getContext().registerReceiver(remove_to_playlist_broadcast,remove_to_playlist_filter);
        getContext().registerReceiver(added_to_playlist_broadcast,added_to_playlist_filter);
    }

    @Override
    public void onDestroy() {
        if (added_to_playlist_broadcast != null) {
            getContext().unregisterReceiver(added_to_playlist_broadcast);
            added_to_playlist_broadcast = null;
        }

        if (remove_to_playlist_broadcast != null) {
            getContext().unregisterReceiver(remove_to_playlist_broadcast);
            remove_to_playlist_broadcast = null;
        }

        if (download_completed_broadcast != null) {
            getContext().unregisterReceiver(download_completed_broadcast);
            download_completed_broadcast = null;
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
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
}
