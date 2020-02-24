package com.structurecode.alto.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.structurecode.alto.Download.SongDownloadManager;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Playlist;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;
import com.structurecode.alto.Services.PlayerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.structurecode.alto.Helpers.Utils.COLLECTION_PLAYLISTS;
import static com.structurecode.alto.Helpers.Utils.COLLECTION_SONGS;
import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;
import static com.structurecode.alto.Services.PlayerService.ADD_TO_QUEUE;
import static com.structurecode.alto.Services.PlayerService.AUDIO_EXTRA;
import static com.structurecode.alto.Services.PlayerService.AUDIO_LIST_EXTRA;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_SONG;
import static com.structurecode.alto.Services.PlayerService.PLAY_SONG;
import static com.structurecode.alto.Services.PlayerService.REMOVE;

public class SongAdapter extends FirestoreRecyclerAdapter<Song, SongAdapter.SongViewHolder> {
    Context context;
    boolean is_parent;
    boolean is_downloaded;
    boolean in_library;

    public SongAdapter(@NonNull FirestoreRecyclerOptions<Song> options,Context context, boolean is_parent, boolean in_library) {
        super(options);
        this.context = context;
        this.is_parent=is_parent;
        this.in_library=in_library;
        user = mAuth.getCurrentUser();
    }

    @Override
    protected void onBindViewHolder(@NonNull SongViewHolder holder, int position, @NonNull Song song) {
        if (is_parent) holder.tree.setVisibility(View.GONE);
        else holder.tree.setVisibility(View.VISIBLE);

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        holder.album.setText(song.getAlbum());

        if (song.getUrl()==null || song.getUrl().isEmpty()){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef.child(song.getPath()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    song.setUrl(uri.toString());

                    Map<String, Object> map = new HashMap<>();
                    map.put("url", uri.toString());

                    is_downloaded=SongDownloadManager.getDownloadTracker(context).isDownloaded(Uri.parse(song.getUrl()));
                    if (is_downloaded){
                        holder.downloaded.setVisibility(View.VISIBLE);
                        holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_remove_download);
                    }
                    else {
                        holder.downloaded.setVisibility(View.GONE);
                        holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_download);

                        if (in_library){
                            if (PlayerService.setting.getIs_library_downloaded()== 1){
                                if (song.getUrl()!=null && !song.getUrl().isEmpty()) {
                                    Intent intent2 = new Intent();
                                    intent2.setAction(DOWNLOAD);
                                    intent2.putExtra(AUDIO_EXTRA, song);
                                    context.sendBroadcast(intent2);
                                }
                            }
                        }
                    }

                    if (in_library){
                        db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                                .collection(Utils.COLLECTION_LIBRARY).document(song.getId()).update(map);
                    }else {
                        db.collection(COLLECTION_SONGS).document(song.getId()).update(map);
                    }


                }
            });

        }else {
            is_downloaded=SongDownloadManager.getDownloadTracker(context).isDownloaded(Uri.parse(song.getUrl()));
            if (is_downloaded){
                holder.downloaded.setVisibility(View.VISIBLE);
                holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_remove_download);
            }
            else {
                holder.downloaded.setVisibility(View.GONE);
                holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_download);
                if (in_library){
                    if (PlayerService.setting.getIs_library_downloaded()== 1){
                        if (song.getUrl()!=null && !song.getUrl().isEmpty()) {
                            Intent intent2 = new Intent();
                            intent2.setAction(DOWNLOAD);
                            intent2.putExtra(AUDIO_EXTRA, song);
                            context.sendBroadcast(intent2);
                        }
                    }
                }
            }
        }

    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_layout, parent, false);
        SongViewHolder holder = new SongViewHolder(v);
        return holder;
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ConstraintLayout tree;
        ImageButton more;
        TextView title;
        TextView artist;
        TextView album;
        ImageButton downloaded;
        PopupMenu popup;

        public SongViewHolder(View itemView) {
            super(itemView);
            tree=itemView.findViewById(R.id.tree);
            more=(ImageButton)itemView.findViewById(R.id.moreItemSong);
            title=(TextView) itemView.findViewById(R.id.songTitle);
            artist=(TextView) itemView.findViewById(R.id.songArtist);
            album=(TextView) itemView.findViewById(R.id.songAlbum);
            downloaded=itemView.findViewById(R.id.downloaded_mark);

            popup=new PopupMenu(context,more);
            popup.inflate(R.menu.menu_song_options);

            itemView.setOnClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int clickedPosition=getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                if (v.getId() == more.getId()) {
                    Song song = getSnapshots().getSnapshot(clickedPosition).toObject(Song.class);
                    popup.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case R.id.addQueue:
                                if (song.getUrl()!=null && !song.getUrl().isEmpty()) {
                                    Intent intent1 = new Intent();
                                    intent1.setAction(ADD_TO_QUEUE);
                                    intent1.putExtra(AUDIO_EXTRA, song);
                                    context.sendBroadcast(intent1);
                                }
                                break;
                            case R.id.download:
                                if (song.getUrl()!=null && !song.getUrl().isEmpty()) {
                                    Intent intent2 = new Intent();
                                    intent2.setAction(DOWNLOAD_SONG);
                                    intent2.putExtra(AUDIO_EXTRA, song);
                                    context.sendBroadcast(intent2);
                                }
                                break;
                            case R.id.addLibrary:
                                db.collection(Utils.COLLECTION_USERS).document(user.getUid()).collection(Utils.COLLECTION_LIBRARY).document(song.getId()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    if (task.getResult().exists()){
                                                        db.collection(Utils.COLLECTION_USERS).document(user.getUid()).collection(Utils.COLLECTION_LIBRARY)
                                                                .document(getSnapshots().getSnapshot(clickedPosition).getId())
                                                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Intent intent = new Intent();
                                                                intent.setAction(Utils.REMOVED_SONG_TO_LIBRARY);
                                                                context.sendBroadcast(intent);

                                                                if (song.getUrl()!=null && !song.getUrl().isEmpty()) {
                                                                    Intent intent2 = new Intent();
                                                                    intent2.setAction(REMOVE);
                                                                    intent2.putExtra(AUDIO_EXTRA, song);
                                                                    context.sendBroadcast(intent2);
                                                                }
                                                                Toast.makeText(context,R.string.deleted,Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }else {
                                                        db.collection(Utils.COLLECTION_USERS).document(user.getUid()).collection(Utils.COLLECTION_LIBRARY)
                                                                .document(song.getId()).set(song).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Intent intent = new Intent();
                                                                intent.setAction(Utils.ADDED_SONG_TO_LIBRARY);
                                                                context.sendBroadcast(intent);

                                                                if (PlayerService.setting.getIs_library_downloaded()== 1){
                                                                    if (song.getUrl()!=null && !song.getUrl().isEmpty()) {
                                                                        Intent intent2 = new Intent();
                                                                        intent2.setAction(DOWNLOAD);
                                                                        intent2.putExtra(AUDIO_EXTRA, song);
                                                                        context.sendBroadcast(intent2);
                                                                    }
                                                                }
                                                                Toast.makeText(context,R.string.added,Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }else {

                                                }
                                            }
                                        });
                                break;
                            case R.id.addPlaylist:
                                ArrayList<Playlist> list = new ArrayList<>();
                                db= FirebaseFirestore.getInstance();
                                user = mAuth.getCurrentUser();

                                db.collection(Utils.COLLECTION_PLAYLISTS).whereEqualTo("user_id",user.getUid())
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            return;
                                        } else {

                                            /*List<Playlist> l = queryDocumentSnapshots.toObjects(Playlist.class);
                                            list.addAll(l);*/

                                            ArrayList<String> titles = new ArrayList<>();
                                            ArrayList<String> ids = new ArrayList<>();
                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                titles.add((String) documentSnapshot.get("title"));
                                                ids.add(documentSnapshot.getId());
                                            }

                                            View view = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_playlist_add, null);
                                            ListView listView = (ListView) view.findViewById(R.id.playlist_add);
                                            ArrayAdapter arrayAdapter = new ArrayAdapter(context,android.R.layout.simple_list_item_1,titles);
                                            listView.setAdapter(arrayAdapter);
                                            AlertDialog dialog = new AlertDialog.Builder(context,R.style.DialogTheme)
                                                    .setTitle(R.string.addPlaylist)
                                                    .setView(view)
                                                    .create();
                                            dialog.show();

                                            Song playlist_song = getSnapshots().getSnapshot(clickedPosition).toObject(Song.class);
                                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    //playlist_song.setId(getSnapshots().getSnapshot(clickedPosition).getId());
                                                    playlist_song.setPlaylist_id(ids.get(position));
                                                    db.collection(Utils.COLLECTION_PLAYLISTS).document(ids.get(position))
                                                            .collection(COLLECTION_SONGS).document(getSnapshots().getSnapshot(clickedPosition).getId())
                                                            .set(playlist_song)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("ABC", "DocumentSnapshot successfully written!");
                                                                    Intent intent = new Intent();
                                                                    intent.setAction(Utils.ADDED_TO_PLAYLIST);
                                                                    context.sendBroadcast(intent);

                                                                    if (PlayerService.setting.getIs_playlist_downloaded()== 1){
                                                                        if (song.getUrl()!=null && !song.getUrl().isEmpty()) {
                                                                            Intent intent2 = new Intent();
                                                                            intent2.setAction(DOWNLOAD);
                                                                            intent2.putExtra(AUDIO_EXTRA, song);
                                                                            context.sendBroadcast(intent2);
                                                                        }
                                                                    }
                                                                    //dialog.dismiss();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w("ABC", "Error writing document", e);
                                                                    dialog.dismiss();
                                                                }
                                                            });
                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    }
                                });
                                break;
                        }

                        return false;
                    });

                    popup.show();
                } else {
                    Song song = getSnapshots().getSnapshot(clickedPosition).toObject(Song.class);
                    if (song.getUrl()!=null && !song.getUrl().isEmpty()) {

                        ArrayList<Song> list = new ArrayList<>();
                        for (int i=0; i<getSnapshots().size(); i++){
                            list.add(getSnapshots().getSnapshot(i).toObject(Song.class));
                        }

                        Intent intent3 = new Intent();
                        intent3.setAction(PLAY_SONG);
                        intent3.putParcelableArrayListExtra(AUDIO_LIST_EXTRA, list);

                        intent3.putExtra(AUDIO_EXTRA, song);
                        context.sendBroadcast(intent3);
                    }
                }
            }
        }
    }
}