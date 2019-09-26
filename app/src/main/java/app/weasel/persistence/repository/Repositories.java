package app.weasel.persistence.repository;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import app.weasel.persistence.database.ApplicationDatabase;

// Alternatively, we could use Dagger or Koin
public enum Repositories {
    INSTANCE;

    private final ConcurrentHashMap<Class<? extends Repository>, Repository> instanceCache;

    Repositories() {
        instanceCache = new ConcurrentHashMap<>();
    }

    public static <T extends Repository> T of(Class<T> clazz, ApplicationDatabase db) {
        try {
            T instance = (T) INSTANCE.instanceCache.get(clazz);
            if (instance == null) {
                INSTANCE.instanceCache.put(
                    clazz,
                    instance = clazz.getConstructor(ApplicationDatabase.class).newInstance(db)
                );
            }
            return instance;
        } catch (NoSuchMethodException | IllegalAccessException
            | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
