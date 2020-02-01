package com.structurecode.alto.Models;

public class Setting{
    private int is_library_downloaded;
    private int is_playlist_downloaded;
    private int cache_size;

    public Setting() {
    }

    public Setting(int is_library_downloaded, int is_playlist_downloaded, int cache_size) {
        this.is_library_downloaded = is_library_downloaded;
        this.is_playlist_downloaded = is_playlist_downloaded;
        this.cache_size = cache_size;
    }

    public int getIs_library_downloaded() {
        return is_library_downloaded;
    }

    public void setIs_library_downloaded(int is_library_downloaded) {
        this.is_library_downloaded = is_library_downloaded;
    }

    public int getIs_playlist_downloaded() {
        return is_playlist_downloaded;
    }

    public void setIs_playlist_downloaded(int is_playlist_downloaded) {
        this.is_playlist_downloaded = is_playlist_downloaded;
    }

    public int getCache_size() {
        return cache_size;
    }

    public void setCache_size(int cache_size) {
        this.cache_size = cache_size;
    }
}
