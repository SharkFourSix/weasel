package app.weasel.event;

import app.weasel.model.QueueItem;

/**
 * <p>This event is posted from {@link app.weasel.fragment.DownloadsFragment}
 * and handled in {@link app.weasel.service.DownloadService}</p>
 * <p>It is used for controlling the downloads only after they have been queued</p>
 */
public final class DownloadActionEvent implements Event {
    public enum Type {
        /**
         * /**
         * <p>Pause one or more downloads</p>
         */
        pause,
        /**
         * <p>Delete one or more downloads</p>
         */
        delete,
        /**
         * <p>Resume one or more downloads</p>
         */
        resume,
        /**
         * <p>Start one or more downloads</p>
         */
        start
    }

    private final Type eventType;
    private final QueueItem item;

    public DownloadActionEvent(Type type, QueueItem item) {
        this.eventType = type;
        this.item = item;
    }

    public QueueItem getItem() {
        return item;
    }

    public Type getEventType() {
        return eventType;
    }
}
