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
import com.structurecode.alto.Models.Setting;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;
import com.structurecode.alto.Services.PlayerService;

import java.util.ArrayList;
import java.util.Collections;
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

/**
 * Created by Guy on 4/17/2017.
 */

public class SongArtistAlbumAdapter extends RecyclerView.Adapter<SongArtistAlbumAdapter.SongViewHolder> {
    List<Song> list = Collections.emptyList();
    Context context;
    boolean is_parent;
    boolean is_downloaded;
    Setting setting;

    public SongArtistAlbumAdapter(List<Song> list, Context context, boolean is_parent,Setting setting) {
        this.list = list;
        this.context = context;
        this.is_parent=is_parent;
        this.setting = setting;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_layout, parent, false);
        SongViewHolder holder = new SongViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        if (is_parent) holder.tree.setVisibility(View.GONE);
        else holder.tree.setVisibility(View.VISIBLE);

        holder.title.setText(list.get(position).getTitle());
        holder.artist.setText(list.get(position).getArtist());
        holder.album.setText(list.get(position).getAlbum());

        if (list.get(position).getUrl()==null || list.get(position).getUrl().isEmpty()){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef.child(list.get(position).getPath()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    list.get(position).setUrl(uri.toString());

                    Map<String, Object> map = new HashMap<>();
                    map.put("url", uri.toString());

                    is_downloaded= SongDownloadManager.getDownloadTracker(context).isDownloaded(Uri.parse(list.get(position).getUrl()));
                    if (is_downloaded){
                        holder.downloaded.setVisibility(View.VISIBLE);
                        holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_remove_download);
                    }
                    else {
                        holder.downloaded.setVisibility(View.GONE);
                        holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_download);
                    }

                    db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                            .collection(Utils.COLLECTION_LIBRARY).document(list.get(position).getId())
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
            });
        }else {
            is_downloaded=SongDownloadManager.getDownloadTracker(context).isDownloaded(Uri.parse(list.get(position).getUrl()));
            if (is_downloaded){
                holder.downloaded.setVisibility(View.VISIBLE);
                holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_remove_download);
            }
            else {
                holder.downloaded.setVisibility(View.GONE);
                holder.popup.getMenu().findItem(R.id.download).setTitle(R.string.action_download);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
                    Song song = list.get(clickedPosition);
                    popup.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case R.id.addQueue:
                                if (song.getUrl() != null && !song.getUrl().isEmpty()) {
                                    Intent intent1 = new Intent();
                                    intent1.setAction(ADD_TO_QUEUE);
                                    intent1.putExtra(AUDIO_EXTRA, song);
                                    context.sendBroadcast(intent1);
                                }
                                break;
                            case R.id.download:
                                if (song.getUrl() != null && !song.getUrl().isEmpty()) {
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
                                                                .document(song.getId())
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

                                                                if (setting.getIs_library_downloaded()== 1){
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
                                db= FirebaseFirestore.getInstance();
                                user = mAuth.getCurrentUser();

                                db.collection(Utils.COLLECTION_PLAYLISTS).whereEqualTo("user_id",user.getUid())
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            return;
                                        } else {

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

                                            Song song = list.get(clickedPosition);
                                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    song.setPlaylist_id(ids.get(position));
                                                    db.collection(COLLECTION_PLAYLISTS).document(ids.get(position))
                                                            .collection(COLLECTION_SONGS).document(song.getId())
                                                            .set(song)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("ABC", "DocumentSnapshot successfully written!");
                                                                    Intent intent = new Intent();
                                                                    intent.setAction(Utils.ADDED_TO_PLAYLIST);
                                                                    context.sendBroadcast(intent);

                                                                    if (setting.getIs_playlist_downloaded()== 1){
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
                    Song song = list.get(clickedPosition);
                    if (song.getUrl() != null && !song.getUrl().isEmpty()) {

                        ArrayList<Song> arrayList = new ArrayList<>();
                        arrayList.addAll(list);

                        Intent intent3 = new Intent();
                        intent3.setAction(PLAY_SONG);
                        intent3.putParcelableArrayListExtra(AUDIO_LIST_EXTRA, arrayList);
                        intent3.putExtra(AUDIO_EXTRA, song);
                        context.sendBroadcast(intent3);
                    }
                }
            }
        }
    }

}
