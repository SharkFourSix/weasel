package app.weasel.persistence.repository;

import app.weasel.model.QueueItem;
import app.weasel.persistence.database.ApplicationDatabase;

public final class QueueRepository extends Repository<QueueItem> {

    public QueueRepository(ApplicationDatabase db) {
        super(db.queueDao());
    }
}
