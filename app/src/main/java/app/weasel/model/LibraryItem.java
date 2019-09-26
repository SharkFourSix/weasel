package app.weasel.model;

import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "library")
public final class LibraryItem extends Item {

    private String filePath;
    private String title, artist, album;
    private long fileSize;
    private long duration;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] albumArt;

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Bindable
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Bindable
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Bindable
    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Bindable
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Bindable
    public byte[] getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }
}