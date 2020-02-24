package com.structurecode.alto.Models;

public class Setting{
    private int is_library_downloaded;
    private int is_playlist_downloaded;
    private int shuffle_mode;
    private int repeat_mode;
    private String license;

    public Setting() {
    }

    public Setting(int is_library_downloaded, int is_playlist_downloaded, int shuffle_mode, int repeat_mode, String license) {
        this.is_library_downloaded = is_library_downloaded;
        this.is_playlist_downloaded = is_playlist_downloaded;
        this.shuffle_mode = shuffle_mode;
        this.repeat_mode = repeat_mode;
        this.license = license;
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

    public int getShuffle_mode() {
        return shuffle_mode;
    }

    public void setShuffle_mode(int shuffle_mode) {
        this.shuffle_mode = shuffle_mode;
    }

    public int getRepeat_mode() {
        return repeat_mode;
    }

    public void setRepeat_mode(int repeat_mode) {
        this.repeat_mode = repeat_mode;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
