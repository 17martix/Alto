package com.structurecode.alto.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {
    private String title;
    private String exposure;

    public Playlist() {
    }

    public Playlist(String title, String exposure) {
        this.title = title;
        this.exposure = exposure;
    }

    protected Playlist(Parcel in) {
        title = in.readString();
        exposure = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(exposure);
    }
}
