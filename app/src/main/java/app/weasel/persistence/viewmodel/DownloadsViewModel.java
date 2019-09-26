package app.weasel.persistence.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import app.weasel.model.DownloadItem;
import app.weasel.persistence.database.ApplicationDatabase;
import app.weasel.persistence.repository.DownloadsRepository;
import app.weasel.persistence.repository.Repositories;

public final class DownloadsViewModel extends BaseViewModel<DownloadItem> {
    public DownloadsViewModel(@NonNull Application application) {
        super(Repositories.of(DownloadsRepository.class,
            ApplicationDatabase.getInstance(application)), application);
    }
}
