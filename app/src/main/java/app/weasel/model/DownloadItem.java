package app.weasel.model;

import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.Ignore;
import app.weasel.persistence.dao.DownloadsDao;
import app.weasel.util.BindingUtils;

@Entity(tableName = "downloads")
public final class DownloadItem extends Item {

    /**
     * The order of these values is important because their ordinals are used for sorting query results
     * in {@link DownloadsDao#stream()}
     */
    public enum DownloadStatus {
        failed,
        downloading,
        paused,
        pending,
        cancelled;

        public static final DownloadStatus[] VALUES = values();
    }

    private String url;
    private String savePath;

    private int downloadId;
    private String fileName;

    private long fileSize;
    private long downloaded;

    private DownloadStatus status = DownloadStatus.pending;

    public DownloadItem() {
    }

    @Ignore
    public DownloadItem(long id, long songId, String url, String savePath, String fileName, long fileSize, long downloaded, DownloadStatus status) {
        setId(id);
        setSongId(songId);
        this.url = url;
        this.savePath = savePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.downloaded = downloaded;
        this.status = status;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
    }

    @Bindable
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Bindable
    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    @Bindable
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Bindable
    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Bindable
    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public int getProgress() {
        return BindingUtils.itemProgress(this);
    }
}
