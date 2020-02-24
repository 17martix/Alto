package com.structurecode.alto.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;
import static com.structurecode.alto.Services.PlayerService.setting;

public class ArtistAdapter extends FirestoreRecyclerAdapter<Song, ArtistAdapter.ArtistViewHolder> {
    Context context;
    ArrayList<String> artists;

    public ArtistAdapter(@NonNull FirestoreRecyclerOptions<Song> options, Context context) {
        super(options);
        this.context = context;
        user = mAuth.getCurrentUser();
        artists=new ArrayList<>();
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.artists_layout, parent, false);
        ArtistViewHolder holder = new ArtistViewHolder(v);
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull ArtistViewHolder holder, int position, @NonNull Song song) {

        if (!artists.contains(song.getArtist())) {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            artists.add(song.getArtist());
            holder.artist_name.setText(song.getArtist());

            holder.song_count.setText("");
            /*db.collection(Utils.COLLECTION_USERS).document(user.getUid()).collection(Utils.COLLECTION_LIBRARY)
                    .whereEqualTo("artist",song.getArtist()).orderBy("title",Query.Direction.ASCENDING)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        ArrayList<Song> list=new ArrayList<>();
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            list.add(snapshot.toObject(Song.class));
                            Log.e("Hello","Hello "+list.size()+"  -----  "+list.get(0).getTitle() );
                        }

                        if (list!=null && !list.isEmpty()) {
                            String number_songs = "";
                            int songs_count = list.size();
                            if (songs_count == 1)
                                number_songs = "1 " + context.getString(R.string.song_count);
                            else number_songs = songs_count + " " + context.getString(R.string.song_counts);
                            holder.song_count.setText(number_songs);
                        }

                        holder.artist_songs_recycler_view.setVisibility(View.GONE);
                        SongArtistAlbumAdapter songArtistAlbumAdapter =new SongArtistAlbumAdapter(list,context,false);
                        holder.artist_songs_recycler_view.setHasFixedSize(true);
                        holder.artist_songs_recycler_view.setLayoutManager(new LinearLayoutManager(context));
                        holder.artist_songs_recycler_view.setAdapter(songArtistAlbumAdapter);
                    } else {
                        Log.d("HELLO", "Error getting documents: ", task.getException());
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Hello Artist", "Hello Artist "+e.getMessage());
                }
            });*/

            db.collection(Utils.COLLECTION_USERS).document(user.getUid()).collection(Utils.COLLECTION_LIBRARY)
                    .whereEqualTo("artist",song.getArtist()).whereArrayContains("license",setting.getLicense())
                    .orderBy("title",Query.Direction.ASCENDING)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    ArrayList<Song> list=new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        list.add(snapshot.toObject(Song.class));
                    }

                    if (list!=null && !list.isEmpty()) {
                        String number_songs = "";
                        int songs_count = queryDocumentSnapshots.size();
                        if (songs_count == 1)
                            number_songs = "1 " + context.getString(R.string.song_count);
                        else number_songs = songs_count + " " + context.getString(R.string.song_counts);
                        holder.song_count.setText(number_songs);
                    }

                    holder.artist_songs_recycler_view.setVisibility(View.GONE);
                    SongArtistAlbumAdapter songArtistAlbumAdapter =new SongArtistAlbumAdapter(list,context,false);
                    holder.artist_songs_recycler_view.setHasFixedSize(true);
                    holder.artist_songs_recycler_view.setLayoutManager(new LinearLayoutManager(context));
                    holder.artist_songs_recycler_view.setAdapter(songArtistAlbumAdapter);
                }
            });
        }else {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }


    class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView artist_name;
        TextView song_count;
        RecyclerView artist_songs_recycler_view;

        ArtistViewHolder(View itemView) {
            super(itemView);
            artist_name=(TextView) itemView.findViewById(R.id.artistName);
            song_count=(TextView) itemView.findViewById(R.id.songCount);
            artist_songs_recycler_view =(RecyclerView) itemView.findViewById(R.id.artist_songs);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition=getAdapterPosition();
            if (artist_songs_recycler_view.getVisibility()==View.VISIBLE){
                artist_songs_recycler_view.setVisibility(View.GONE);
            }else {
                artist_songs_recycler_view.setVisibility(View.VISIBLE);
            }
        }
    }
}
