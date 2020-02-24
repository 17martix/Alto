package com.structurecode.alto.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Playlist implements Parcelable {
    private String id;
    private String title;
    private String exposure;
    private String genre;
    private String mood;
    private String user_id;
    private String user_name;
    private List<String> followers;

    public Playlist() {
    }

    public Playlist(String id, String title, String exposure, String genre, String mood,
                    String user_id, String user_name, List<String> followers) {
        this.id = id;
        this.title = title;
        this.exposure = exposure;
        this.genre = genre;
        this.mood = mood;
        this.user_id = user_id;
        this.user_name = user_name;
        this.followers = followers;
    }

    public Playlist(String id, String title, String exposure, String user_id, String user_name,
                    List<String> followers) {
        this.id = id;
        this.title = title;
        this.exposure = exposure;
        this.user_id = user_id;
        this.user_name = user_name;
        this.followers = followers;
    }

    protected Playlist(Parcel in) {
        id = in.readString();
        title = in.readString();
        exposure = in.readString();
        genre = in.readString();
        mood = in.readString();
        user_id = in.readString();
        user_name = in.readString();
        followers = in.createStringArrayList();
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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
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
        dest.writeString(user_id);
        dest.writeString(user_name);
        dest.writeStringList(followers);
    }
}
