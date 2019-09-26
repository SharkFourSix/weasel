package app.weasel.persistence.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import app.weasel.model.QueueItem;
import app.weasel.persistence.database.ApplicationDatabase;
import app.weasel.persistence.repository.QueueRepository;
import app.weasel.persistence.repository.Repositories;

public final class QueueItemViewModel extends BaseViewModel<QueueItem> {
    public QueueItemViewModel(@NonNull Application application) {
        super(Repositories.of(QueueRepository.class,
            ApplicationDatabase.getInstance(application)), application);
    }
}
