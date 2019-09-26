package app.weasel.event;

public final class NewQueueItemEvent {
    private final long[] ids;

    public NewQueueItemEvent(long[] ids) {
        this.ids = ids;
    }

    public long[] getIds() {
        return ids;
    }
}
