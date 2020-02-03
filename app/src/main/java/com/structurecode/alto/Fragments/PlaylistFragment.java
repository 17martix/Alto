package com.structurecode.alto.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.structurecode.alto.Adapters.PlaylistAdapter;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Playlist;
import com.structurecode.alto.R;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.user;

/**
 * Created by Guy on 4/23/2017.
 */

public class PlaylistFragment extends Fragment {
    private PlaylistAdapter adapter;
    private RecyclerView recyclerView;

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
        adapter.stopListening();
    }

}
