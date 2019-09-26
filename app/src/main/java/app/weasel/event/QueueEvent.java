package app.weasel.event;

import java.util.ArrayList;
import java.util.List;

import app.weasel.model.QueueItem;

public class QueueEvent implements Event {
    public enum Type {
        download_item,
        delete_items
    }

    private final List<QueueItem> items;
    private final Type type;

    public QueueEvent(Type type, QueueItem item) {
        this(type, new ArrayList<QueueItem>() {{
            add(item);
        }});
    }

    public QueueEvent(Type type, List<QueueItem> items) {
        this.type = type;
        this.items = items;
    }

    public Type getType() {
        return type;
    }

    public List<QueueItem> getItems() {
        return items;
    }

    public QueueItem getItem() {
        return getItems().get(0);
    }
}
