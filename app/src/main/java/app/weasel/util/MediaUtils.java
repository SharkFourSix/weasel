package app.weasel.util;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;

public enum MediaUtils {
    ;
    private static final String TAG = "MediaUtils";

    private static final SongInfo EMPTY_INFO = new SongInfo(
        null,
        null,
        null,
        null,
        0
    );

    private static String propertyOrUnknown(String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static class SongInfo {
        private String title, album, artist;
        private byte[] albumArt;
        private long duration;

        SongInfo(String title, String album, String artist, byte[] albumArt, long duration) {
            this.title = propertyOrUnknown(title, "<Unknown Title>");
            this.album = propertyOrUnknown(album, "<Unknown Album>");
            this.artist = propertyOrUnknown(artist, "<Unknown Artist>");
            this.albumArt = albumArt;
            this.duration = duration;
        }

        public String getTitle() {
            return title;
        }

        public String getAlbum() {
            return album;
        }

        public String getArtist() {
            return artist;
        }

        public byte[] getAlbumArt() {
            return albumArt;
        }

        public long getDuration() {
            return duration;
        }
    }

    @NonNull
    public static SongInfo getLocalFileMetadata(String path) {
        final MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        try {
            metadataRetriever.setDataSource(path);
            return new SongInfo(
                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                metadataRetriever.getEmbeddedPicture(),
                Long.valueOf(metadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
            );
        } catch (Exception e) {
            Log.e(TAG, "Exception during metadata retrieval", e);
        } finally {
            metadataRetriever.release();
        }
        return EMPTY_INFO;
    }
}
