package app.weasel.persistence.repository;

import app.weasel.model.DownloadItem;
import app.weasel.persistence.database.ApplicationDatabase;

public final class DownloadsRepository extends Repository<DownloadItem> {

    public DownloadsRepository(ApplicationDatabase database) {
        super(database.downloadsDao());
    }
}