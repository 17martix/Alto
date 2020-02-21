package com.structurecode.alto.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {
    private String id;
    private String title;
    private String exposure;
    private String genre;
    private String mood;

    public Playlist() {
    }

    public Playlist(String id, String title, String exposure, String genre, String mood) {
        this.id = id;
        this.title = title;
        this.exposure = exposure;
        this.genre = genre;
        this.mood = mood;
    }

    public Playlist(String id, String title, String exposure) {
        this.id = id;
        this.title = title;
        this.exposure = exposure;
    }

    protected Playlist(Parcel in) {
        id = in.readString();
        title = in.readString();
        exposure = in.readString();
        genre = in.readString();
        mood = in.readString();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
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

    public String getExposure() {
        return exposure;
    }

    public void setExposure(String exposure) {
        this.exposure = exposure;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(exposure);
        dest.writeString(genre);
        dest.writeString(mood);
    }
}
