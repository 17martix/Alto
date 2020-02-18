package com.structurecode.alto.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.structurecode.alto.Helpers.Utils;

public class Song implements Parcelable {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String path;
    private String url;
    private String lyrics;
    private String playlist_id;

    public Song() {
    }

    public Song(String id, String title, String artist, String album, String path, String url, String lyrics, String playlist_id) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.url = url;
        this.lyrics = lyrics;
        this.playlist_id = playlist_id;
    }

    public Song(String id, String title, String artist, String album, String path, String url, String lyrics) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.url = url;
        this.lyrics = lyrics;
    }

    public Song(String id, String title, String artist, String album, String path, String url) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.url = url;
    }

    protected Song(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        path = in.readString();
        url = in.readString();
        lyrics = in.readString();
        playlist_id = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getPlaylist_id() {
        return playlist_id;
    }

    public void setPlaylist_id(String playlist_id) {
        this.playlist_id = playlist_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(path);
        dest.writeString(url);
        dest.writeString(lyrics);
        dest.writeString(playlist_id);
    }
}
