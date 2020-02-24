package com.structurecode.alto.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.structurecode.alto.Helpers.Utils;

import java.util.List;

public class Song implements Parcelable {
    private String id;
    private String title;
    private String artist;
    private String album;
    private int year;
    private String genre;
    private String mood;
    private int track;
    private int bpm;
    private String label;
    private String releasecountry;
    private String acoustid_id;
    private String path;
    private List<String> license;
    private String url;
    private String lyrics;
    private String playlist_id;

    public Song() {
    }

    public Song(String id, String title, String artist, String album, int year, String genre,
                String mood, int track, int bpm, String label, String releasecountry, String acoustid_id,
                String path, List<String> license, String url, String lyrics, String playlist_id) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.genre = genre;
        this.mood = mood;
        this.track = track;
        this.bpm = bpm;
        this.label = label;
        this.releasecountry = releasecountry;
        this.acoustid_id = acoustid_id;
        this.path = path;
        this.license = license;
        this.url = url;
        this.lyrics = lyrics;
        this.playlist_id = playlist_id;
    }

    protected Song(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        year = in.readInt();
        genre = in.readString();
        mood = in.readString();
        track = in.readInt();
        bpm = in.readInt();
        label = in.readString();
        releasecountry = in.readString();
        acoustid_id = in.readString();
        path = in.readString();
        license = in.createStringArrayList();
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getReleasecountry() {
        return releasecountry;
    }

    public void setReleasecountry(String releasecountry) {
        this.releasecountry = releasecountry;
    }

    public String getAcoustid_id() {
        return acoustid_id;
    }

    public void setAcoustid_id(String acoustid_id) {
        this.acoustid_id = acoustid_id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getLicense() {
        return license;
    }

    public void setLicense(List<String> license) {
        this.license = license;
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
        dest.writeInt(year);
        dest.writeString(genre);
        dest.writeString(mood);
        dest.writeInt(track);
        dest.writeInt(bpm);
        dest.writeString(label);
        dest.writeString(releasecountry);
        dest.writeString(acoustid_id);
        dest.writeString(path);
        dest.writeStringList(license);
        dest.writeString(url);
        dest.writeString(lyrics);
        dest.writeString(playlist_id);
    }
}
