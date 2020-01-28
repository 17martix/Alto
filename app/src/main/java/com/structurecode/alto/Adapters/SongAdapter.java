package com.structurecode.alto.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.Collections;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    List<Song> list = Collections.emptyList();
    Context context;
    boolean is_parent;

    public SongAdapter(List<Song> list, Context context, boolean is_parent) {
        this.list = list;
        this.context = context;
        this.is_parent=is_parent;
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

        SongViewHolder(View itemView) {
            super(itemView);
            tree=itemView.findViewById(R.id.tree);
            more=(ImageButton)itemView.findViewById(R.id.moreItemSong);
            title=(TextView) itemView.findViewById(R.id.songTitle);
            artist=(TextView) itemView.findViewById(R.id.songArtist);
            album=(TextView) itemView.findViewById(R.id.songAlbum);

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
                                break;
                            case R.id.download:

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
