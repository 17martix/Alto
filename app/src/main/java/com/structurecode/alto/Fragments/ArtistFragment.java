package com.structurecode.alto.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.structurecode.alto.Adapters.ArtistAdapter;
import com.structurecode.alto.Adapters.RecyclerViewEmptySupport;
import com.structurecode.alto.ExploreActivity;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Setting;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.user;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_COMPLETED;

/**
 * Created by Guy on 4/23/2017.
 */

public class ArtistFragment extends Fragment {
    private ArtistAdapter adapter;
    private RecyclerViewEmptySupport recyclerView;
    private Button empty_button;
    private LinearLayout empty_view;
    private BroadcastReceiver added_artist_song_broadcast;
    private BroadcastReceiver remove_artist_song_broadcast;
    private BroadcastReceiver download_completed_broadcast;
    private Setting setting;

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_library_artists, container, false);

        recyclerView = view.findViewById(R.id.ArtistList);
        empty_view = view.findViewById(R.id.empty_view);
        empty_button = view.findViewById(R.id.empty_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyView(empty_view);

        setting = new Setting(1,1,0,2,"free");
        db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                .collection(Utils.COLLECTION_SETTINGS).document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            setting = documentSnapshot.toObject(Setting.class);
                        }
                        main_query(setting.getLicense());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                main_query(setting.getLicense());
            }
        });

        initialize_broadcasts();

        empty_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getContext(), ExploreActivity.class);
                getActivity().startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void main_query(String license){
        Query query = db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                .collection(Utils.COLLECTION_LIBRARY).whereArrayContains("license",license)
                .orderBy("artist",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Song> options = new FirestoreRecyclerOptions.Builder<Song>()
                .setQuery(query, Song.class)
                .build();
        adapter = new ArtistAdapter(options,getContext(),setting);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public void initialize_broadcasts(){
        IntentFilter added_artist_song_filter = new IntentFilter();
        added_artist_song_filter.addAction(Utils.ADDED_SONG_TO_LIBRARY);
        added_artist_song_broadcast =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter remove_artist_song_filter = new IntentFilter();
        remove_artist_song_filter.addAction(Utils.REMOVED_SONG_TO_LIBRARY);
        remove_artist_song_broadcast =new BroadcastReceiver() {
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
        getContext().registerReceiver(remove_artist_song_broadcast,remove_artist_song_filter);
        getContext().registerReceiver(added_artist_song_broadcast,added_artist_song_filter);
    }

    @Override
    public void onDestroy() {
        if (added_artist_song_broadcast != null) {
            getContext().unregisterReceiver(added_artist_song_broadcast);
            added_artist_song_broadcast = null;
        }

        if (remove_artist_song_broadcast != null) {
            getContext().unregisterReceiver(remove_artist_song_broadcast);
            remove_artist_song_broadcast = null;
        }

        if (download_completed_broadcast != null) {
            getContext().unregisterReceiver(download_completed_broadcast);
            download_completed_broadcast = null;
        }
        super.onDestroy();
    }

    /*@Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        //adapter.stopListening();
    }*/

}
