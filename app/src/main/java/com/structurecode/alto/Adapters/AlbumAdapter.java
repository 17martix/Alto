package com.structurecode.alto.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Setting;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;

public class AlbumAdapter extends FirestoreRecyclerAdapter<Song, AlbumAdapter.AlbumViewHolder> {
    Context context;
    ArrayList<String> albums;
    Setting setting;

    public AlbumAdapter(@NonNull FirestoreRecyclerOptions<Song> options, Context context,Setting setting) {
        super(options);
        this.context = context;
        user = mAuth.getCurrentUser();
        albums=new ArrayList<>();
        this.setting = setting;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.albums_layout, parent, false);
        AlbumViewHolder holder = new AlbumViewHolder(v);
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull AlbumViewHolder holder, int position, @NonNull Song song) {
        if (!albums.contains(song.getAlbum())) {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            albums.add(song.getAlbum());
            holder.album_name.setText(song.getAlbum());
            holder.artist_name.setText(song.getArtist());

            holder.song_count.setText("");
            db.collection(Utils.COLLECTION_USERS).document(user.getUid()).collection(Utils.COLLECTION_LIBRARY)
                    .whereEqualTo("album",song.getAlbum()).whereArrayContains("license",setting.getLicense())
                    .orderBy("title", Query.Direction.ASCENDING)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    ArrayList<Song> list=new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        list.add(snapshot.toObject(Song.class));
                        Log.e("Hello","Hello "+list.size()+"  -----  "+list.get(0).getTitle() );
                    }

                    if (list!=null && !list.isEmpty()) {
                        String number_songs = "";
                        int songs_count = queryDocumentSnapshots.size();
                        if (songs_count == 1)
                            number_songs = "1 " + context.getString(R.string.song_count);
                        else number_songs = songs_count + " " + context.getString(R.string.song_counts);
                        holder.song_count.setText(number_songs);
                    }

                    holder.album_songs_recycler_view.setVisibility(View.GONE);
                    SongArtistAlbumAdapter songArtistAlbumAdapter =new SongArtistAlbumAdapter(list,context,false,setting);
                    holder.album_songs_recycler_view.setHasFixedSize(true);
                    holder.album_songs_recycler_view.setLayoutManager(new LinearLayoutManager(context));
                    holder.album_songs_recycler_view.setAdapter(songArtistAlbumAdapter);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Hello Album", "Hello Album "+e.getMessage());
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

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView artist_name;
        TextView album_name;
        TextView song_count;
        RecyclerView album_songs_recycler_view;

        AlbumViewHolder(View itemView) {
            super(itemView);
            artist_name=(TextView) itemView.findViewById(R.id.artistName);
            album_name=(TextView) itemView.findViewById(R.id.albumName);
            song_count=(TextView) itemView.findViewById(R.id.songCount);
            album_songs_recycler_view =(RecyclerView) itemView.findViewById(R.id.album_songs);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition=getAdapterPosition();
            if (album_songs_recycler_view.getVisibility()==View.VISIBLE){
                album_songs_recycler_view.setVisibility(View.GONE);
            }else {
                album_songs_recycler_view.setVisibility(View.VISIBLE);
            }
        }
    }
}
