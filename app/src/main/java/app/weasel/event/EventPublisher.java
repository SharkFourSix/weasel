package app.weasel.event;

import org.greenrobot.eventbus.EventBus;

public enum EventPublisher {
    ;

    public static <T extends Event> void publish(T event) {
        EventBus.getDefault().post(event);
    }

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }
}
