package com.structurecode.alto.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.structurecode.alto.Download.SongDownloadManager;
import com.structurecode.alto.Download.SongDownloadTracker;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;

import static com.structurecode.alto.Services.PlayerService.ADD_TO_QUEUE;
import static com.structurecode.alto.Services.PlayerService.AUDIO_EXTRA;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_SONG;
import static com.structurecode.alto.Services.PlayerService.PLAY_SONG;

public class SongAdapter extends FirestoreRecyclerAdapter<Song, SongAdapter.SongViewHolder> {
    Context context;
    private SongDownloadManager songDownloadManager;
    SongDownloadTracker songDownloadTracker;
    boolean is_parent;
    boolean is_downloaded;

    public SongAdapter(@NonNull FirestoreRecyclerOptions<Song> options,Context context, boolean is_parent) {
        super(options);
        this.context = context;
        this.is_parent=is_parent;
        songDownloadManager = new SongDownloadManager(context);
        songDownloadTracker=songDownloadManager.getDownloadTracker();
    }

    @Override
    protected void onBindViewHolder(@NonNull SongViewHolder holder, int position, @NonNull Song song) {
        if (is_parent) holder.tree.setVisibility(View.GONE);
        else holder.tree.setVisibility(View.VISIBLE);

        is_downloaded=songDownloadTracker.isDownloaded(Uri.parse(song.getUrl()));
        if (is_downloaded){
            holder.downloaded.setVisibility(View.VISIBLE);
            holder.popup.getMenu().getItem(R.id.download).setTitle(R.string.action_remove_download);
        }
        else {
            holder.downloaded.setVisibility(View.GONE);
            holder.popup.getMenu().getItem(R.id.download).setTitle(R.string.action_download);
        }

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        holder.album.setText(song.getAlbum());
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
                                Intent intent1 = new Intent();
                                intent1.setAction(ADD_TO_QUEUE);
                                intent1.putExtra(AUDIO_EXTRA, song);
                                context.sendBroadcast(intent1);
                                break;
                            case R.id.download:
                                Intent intent2 = new Intent();
                                intent2.setAction(DOWNLOAD_SONG);
                                intent2.putExtra(AUDIO_EXTRA, song);
                                context.sendBroadcast(intent2);
                                break;
                            case R.id.addLibrary:
                                break;
                            case R.id.addPlaylist:
                                break;
                        }

                        return false;
                    });

                    popup.show();
                } else {
                    ArrayList<Song> list = new ArrayList<>();
                    for (int i=0; i<getSnapshots().size(); i++){
                        list.add(getSnapshots().getSnapshot(i).toObject(Song.class));
                    }

                    Intent intent3 = new Intent();
                    intent3.setAction(PLAY_SONG);
                    intent3.putParcelableArrayListExtra(AUDIO_EXTRA,list);
                    intent3.putExtra(AUDIO_EXTRA, getSnapshots().getSnapshot(clickedPosition).toObject(Song.class));
                    context.sendBroadcast(intent3);
                }
            }
        }
    }
}

/*public class SongAdapter {
    private List<Song> list = Collections.emptyList();
    private Context context;
    private SongDownloadManager songDownloadManager;
    private SongDownloadTracker songDownloadTracker;
    private boolean is_parent;
    private boolean is_downloaded;
    private Song selected_song;

    private Query mQuery;
    private FirestoreRecyclerAdapter<Song,SongViewHolder> mAdapter;
    private FirestoreRecyclerOptions<Song> options;

    public void update(){
        mAdapter = new FirestoreRecyclerAdapter<Song, SongViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SongViewHolder holder, int position, @NonNull Song song) {
                if (is_parent) holder.tree.setVisibility(View.GONE);
                else holder.tree.setVisibility(View.VISIBLE);

                is_downloaded=songDownloadTracker.isDownloaded(Uri.parse(song.getUrl()));
                if (is_downloaded){
                    holder.downloaded.setVisibility(View.VISIBLE);
                    holder.popup.getMenu().getItem(R.id.download).setTitle(R.string.action_remove_download);
                }
                else {
                    holder.downloaded.setVisibility(View.GONE);
                    holder.popup.getMenu().getItem(R.id.download).setTitle(R.string.action_download);
                }

                holder.title.setText(song.getTitle());
                holder.artist.setText(song.getArtist());
                holder.album.setText(song.getAlbum());
            }

            @NonNull
            @Override
            public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_layout, parent, false);
                SongViewHolder holder = new SongViewHolder(v);
                return holder;
            }
        };
    }

    private class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ConstraintLayout tree;
        ImageButton more;
        TextView title;
        TextView artist;
        TextView album;
        ImageButton downloaded;
        PopupMenu popup;

        SongViewHolder(View itemView) {
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
            if (v.getId() == more.getId()){
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.addQueue:
                                Intent intent1=new Intent();
                                intent1.setAction(ADD_TO_QUEUE);
                                intent1.putExtra(AUDIO_EXTRA,list.get(clickedPosition));
                                context.sendBroadcast(intent1);
                                break;
                            case R.id.download:
                                Intent intent2=new Intent();
                                intent2.setAction(DOWNLOAD_SONG);
                                intent2.putExtra(AUDIO_EXTRA,so);
                                context.sendBroadcast(intent2);
                                break;
                            case R.id.addLibrary:
                                break;
                            case R.id.addPlaylist:
                                break;
                        }

                        return false;
                    }
                });

                popup.show();
            }else {

            }
        }
    }
}*/

/*
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    List<Song> list = Collections.emptyList();
    Context context;
    private SongDownloadManager songDownloadManager;
    SongDownloadTracker songDownloadTracker;
    boolean is_parent;
    boolean is_downloaded;

    public SongAdapter(List<Song> list, Context context, boolean is_parent) {
        this.list = list;
        this.context = context;
        this.is_parent=is_parent;
        songDownloadManager = new SongDownloadManager(context);
        songDownloadTracker=songDownloadManager.getDownloadTracker();
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_layout, parent, false);
        SongViewHolder holder = new SongViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        if (is_parent) holder.tree.setVisibility(View.VISIBLE);
        else holder.tree.setVisibility(View.GONE);

        is_downloaded=songDownloadTracker.isDownloaded(Uri.parse(list.get(position).getUrl()));
        if (is_downloaded){
            holder.downloaded.setVisibility(View.VISIBLE);
            holder.popup.getMenu().getItem(R.id.download).setTitle(R.string.action_remove_download);
        }
        else {
            holder.downloaded.setVisibility(View.GONE);
            holder.popup.getMenu().getItem(R.id.download).setTitle(R.string.action_download);
        }

        holder.title.setText(list.get(position).getTitle());
        holder.artist.setText(list.get(position).getArtist());
        holder.album.setText(list.get(position).getAlbum());
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

        SongViewHolder(View itemView) {
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
            if (v.getId() == more.getId()){
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.addQueue:
                                Intent intent1=new Intent();
                                intent1.setAction(ADD_TO_QUEUE);
                                intent1.putExtra(AUDIO_EXTRA,list.get(clickedPosition));
                                context.sendBroadcast(intent1);
                                break;
                            case R.id.download:
                                Intent intent2=new Intent();
                                intent2.setAction(DOWNLOAD_SONG);
                                intent2.putExtra(AUDIO_EXTRA,list.get(clickedPosition));
                                context.sendBroadcast(intent2);
                                break;
                            case R.id.addLibrary:
                                break;
                            case R.id.addPlaylist:
                                break;
                        }

                        return false;
                    }
                });

                popup.show();
            }else {

            }
        }
    }

}
*/
