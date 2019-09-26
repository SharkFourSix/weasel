package app.weasel.persistence.dao.converter;

import java.util.Date;

import androidx.room.TypeConverter;
import app.weasel.model.DownloadItem;

public final class GenericConverters {
    @TypeConverter
    public Date timestampToDate(long time) {
        return new Date(time);
    }

    @TypeConverter
    public long dateToTimestamp(Date date) {
        return date == null ? 0 : date.getTime();
    }

    @TypeConverter
    public DownloadItem.DownloadStatus ordinalToStatus(int ordinal) {
        return DownloadItem.DownloadStatus.VALUES[ordinal];
    }

    @TypeConverter
    public int statusToOrdinal(DownloadItem.DownloadStatus status) {
        return status != null ? status.ordinal() : DownloadItem.DownloadStatus.pending.ordinal();
    }
}
