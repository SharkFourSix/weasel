package app.weasel.persistence.viewmodel;

import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import app.weasel.model.Item;

public enum ViewModels {
    $;

    private final ConcurrentHashMap<Class<? extends BaseViewModel>, BaseViewModel> cache
        = new ConcurrentHashMap<>();

    public static <T extends Item, M extends BaseViewModel<T>> M get(Class<M> klazz, Application application) {
        M instance = klazz.cast($.cache.get(klazz));
        if (instance == null) {
            try {
                $.cache.put(
                    klazz,
                    instance = klazz.getConstructor(Application.class).newInstance(application)
                );
            } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
}
