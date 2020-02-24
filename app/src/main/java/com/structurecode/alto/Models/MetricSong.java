package com.structurecode.alto.Models;

import java.util.List;
import java.util.Map;

public class MetricSong extends Song {
    private Map<String, Long> users;
    private String daily_play;
    private String monthly_play;
    private String yearly_play;

    public MetricSong() {
    }

    public MetricSong(String id, String title, String artist, String album, int year, String genre,
                      String mood, int track, int bpm, String label, String releasecountry,
                      String acoustid_id, String path, List<String> license, String url, String lyrics,
                      String playlist_id, Map<String, Long> users, String daily_play, String monthly_play,
                      String yearly_play) {
        super(id, title, artist, album, year, genre, mood, track, bpm, label, releasecountry,
                acoustid_id, path, license, url, lyrics, playlist_id);
        this.users = users;
        this.daily_play = daily_play;
        this.monthly_play = monthly_play;
        this.yearly_play = yearly_play;
    }

    public MetricSong(Song song, Map<String, Long> users, String daily_play, String monthly_play,
                      String yearly_play) {
        super(song.getId(), song.getTitle(), song.getArtist(), song.getAlbum(), song.getYear(),
                song.getGenre(), song.getMood(), song.getTrack(), song.getBpm(), song.getLabel(),
                song.getReleasecountry(), song.getAcoustid_id(), song.getPath(), song.getLicense(),
                song.getUrl(), song.getLyrics(), song.getPlaylist_id());
        this.users = users;
        this.daily_play = daily_play;
        this.monthly_play = monthly_play;
        this.yearly_play = yearly_play;
    }

    public Map<String, Long> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Long> users) {
        this.users = users;
    }

    public String getDaily_play() {
        return daily_play;
    }

    public void setDaily_play(String daily_play) {
        this.daily_play = daily_play;
    }

    public String getMonthly_play() {
        return monthly_play;
    }

    public void setMonthly_play(String monthly_play) {
        this.monthly_play = monthly_play;
    }

    public String getYearly_play() {
        return yearly_play;
    }

    public void setYearly_play(String yearly_play) {
        this.yearly_play = yearly_play;
    }
}
