package com.structurecode.alto.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.structurecode.alto.Models.Playlist;
import com.structurecode.alto.Models.Setting;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.structurecode.alto.Helpers.Utils.COLLECTION_SONGS;
import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;

public class PlaylistAdapter extends FirestoreRecyclerAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder> {
    Context context;
    Setting setting;

    public PlaylistAdapter(@NonNull FirestoreRecyclerOptions<Playlist> options, Context context,Setting setting) {
        super(options);
        this.context = context;
        user = mAuth.getCurrentUser();
        this.setting = setting;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_layout, parent, false);
        PlaylistViewHolder holder = new PlaylistViewHolder(v);
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position, @NonNull Playlist playlist) {
        holder.playlist_name.setText(playlist.getTitle());

        /*Query query = db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                .collection(Utils.COLLECTION_PLAYLISTS).document(getSnapshots().getSnapshot(position).getId())
                .collection(Utils.COLLECTION_SONGS)
                .orderBy("title",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Song> options = new FirestoreRecyclerOptions.Builder<Song>()
                .setQuery(query, Song.class)
                .build();
        holder.playlist_songs_recycler_view.setVisibility(View.VISIBLE);
        SongAdapter songAdapter = new SongAdapter(options,context,false);
        holder.playlist_songs_recycler_view.setHasFixedSize(true);
        holder.playlist_songs_recycler_view.setLayoutManager(new LinearLayoutManager(context));
        holder.playlist_songs_recycler_view.setAdapter(songAdapter);*/

        holder.song_count.setText("");
        db.collection(Utils.COLLECTION_PLAYLISTS).document(getSnapshots().getSnapshot(position).getId())
                .collection(COLLECTION_SONGS).whereArrayContains("license",setting.getLicense())
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

                holder.playlist_songs_recycler_view.setVisibility(View.GONE);
                SongPlaylistAdapter songPlaylistAdapter =new SongPlaylistAdapter(list,context,false);
                holder.playlist_songs_recycler_view.setHasFixedSize(true);
                holder.playlist_songs_recycler_view.setLayoutManager(new LinearLayoutManager(context));
                holder.playlist_songs_recycler_view.setAdapter(songPlaylistAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ABC",e.getMessage());
            }
        });
    }



    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageButton more;
        TextView playlist_name;
        TextView song_count;
        RecyclerView playlist_songs_recycler_view;

        PlaylistViewHolder(View itemView) {
            super(itemView);
            more=(ImageButton)itemView.findViewById(R.id.moreItemPlaylist);
            playlist_name=(TextView) itemView.findViewById(R.id.playlistName);
            song_count=(TextView) itemView.findViewById(R.id.songCount);
            playlist_songs_recycler_view=itemView.findViewById(R.id.playlist_songs);

            itemView.setOnClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition=getAdapterPosition();
            if (v.getId() == more.getId()){
                Playlist playlist=getSnapshots().getSnapshot(clickedPosition).toObject(Playlist.class);
                PopupMenu popup=new PopupMenu(context,more);
                popup.inflate(R.menu.menu_playlist_options);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.editPlaylist:
                                View view = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_playlist, null);
                                ((EditText) view.findViewById(R.id.title_txt)).setText(playlist.getTitle());
                                if (playlist.getExposure().equals(context.getString(R.string.exposure_private))){
                                    ((CheckBox) view.findViewById(R.id.exposure)).setChecked(true);
                                }else {
                                    ((CheckBox) view.findViewById(R.id.exposure)).setChecked(false);
                                }

                                AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.DialogTheme)
                                        .setTitle(R.string.editPlaylist)
                                        .setView(view)
                                        .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String exposure=context.getString(R.string.exposure_public);
                                                boolean is_private = ((CheckBox) view.findViewById(R.id.exposure)).isChecked();
                                                if(is_private) exposure=context.getString(R.string.exposure_private);

                                                String inputTitle = ((EditText) view.findViewById(R.id.title_txt)).getText().toString();
                                                if (inputTitle==null || inputTitle.isEmpty()) {
                                                    inputTitle = context.getString(R.string.untitled);
                                                }else {
                                                    inputTitle=inputTitle.trim();
                                                }

                                                Map<String,Object> map = new HashMap<>();
                                                map.put("title",inputTitle);
                                                map.put("exposure", exposure);
                                                db.collection(Utils.COLLECTION_PLAYLISTS).document(getSnapshots()
                                                        .getSnapshot(clickedPosition).getId())
                                                        .update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("ABC", "DocumentSnapshot successfully written!");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("ABC", "Error writing document", e);
                                                            }
                                                        });
                                            }
                                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).create();
                                alertDialog.show();
                                break;
                            case R.id.deletePlaylist:
                                db.collection(Utils.COLLECTION_PLAYLISTS).document(getSnapshots().getSnapshot(clickedPosition).getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("ABC", "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("ABC", "Error writing document", e);
                                            }
                                        });
                                break;
                        }

                        return false;
                    }
                });

                popup.show();
            }else {
                if (playlist_songs_recycler_view.getVisibility()==View.VISIBLE){
                    playlist_songs_recycler_view.setVisibility(View.GONE);
                }else {
                    playlist_songs_recycler_view.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
