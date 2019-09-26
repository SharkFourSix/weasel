package app.weasel.event;

import app.weasel.model.DownloadItem;

public final class DownloadItemEvent implements Event {

    public enum Type {
        download,
        stop
    }

    private final Type type;

    private final DownloadItem item;

    public DownloadItemEvent(Type type, DownloadItem item) {
        this.type = type;
        this.item = item;
    }

    public Type getType() {
        return type;
    }

    public DownloadItem getItem() {
        return item;
    }
}
