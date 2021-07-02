package com.devansh.music;

import android.graphics.Bitmap;

public class AudioModel {
    private String path;
    private String name;
    private String album;
    private String artist;
    private Bitmap cover;
    private long duration;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public long getDuration() { return duration; }

    public void setDuration(long duration){ this.duration = duration;}
}
