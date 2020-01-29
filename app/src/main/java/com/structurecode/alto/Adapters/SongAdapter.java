package com.structurecode.alto.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.structurecode.alto.Download.SongDownloadManager;
import com.structurecode.alto.Download.SongDownloadTracker;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.Collections;
import java.util.List;

import static com.structurecode.alto.Services.PlayerService.ADD_TO_QUEUE;
import static com.structurecode.alto.Services.PlayerService.AUDIO_EXTRA;
import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_SONG;

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
        if (is_downloaded) holder.downloaded.setVisibility(View.VISIBLE);
        else holder.downloaded.setVisibility(View.GONE);

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

        SongViewHolder(View itemView) {
            super(itemView);
            tree=itemView.findViewById(R.id.tree);
            more=(ImageButton)itemView.findViewById(R.id.moreItemSong);
            title=(TextView) itemView.findViewById(R.id.songTitle);
            artist=(TextView) itemView.findViewById(R.id.songArtist);
            album=(TextView) itemView.findViewById(R.id.songAlbum);
            downloaded=itemView.findViewById(R.id.downloaded_mark);

            itemView.setOnClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int clickedPosition=getAdapterPosition();
            if (v.getId() == more.getId()){
                PopupMenu popup=new PopupMenu(context,more);
                popup.inflate(R.menu.menu_song_options);
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
