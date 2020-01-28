package com.structurecode.alto.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private int id;
    private String title;
    private int artist;
    private int album;
    private String url;

    public Song(int id, String title, int artist, int album, String url) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getArtist() {
        return artist;
    }

    public void setArtist(int artist) {
        this.artist = artist;
    }

    public int getAlbum() {
        return album;
    }

    public void setAlbum(int album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    protected Song(Parcel in) {
        id = in.readInt();
        title = in.readString();
        artist = in.readInt();
        album = in.readInt();
        url = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeInt(artist);
        dest.writeInt(album);
        dest.writeString(url);
    }
}
