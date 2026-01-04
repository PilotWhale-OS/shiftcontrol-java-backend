package at.shiftcontrol.shiftservice.event;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BaseEventFiller {
    private BaseEventFiller() {}

    private static final Map<Class<?>, FieldAccess> CACHE = new ConcurrentHashMap<>();

    public static void fill(
        Object event,
        int version,
        String actingUserId,
        String traceId
    ) throws IllegalAccessException {
        FieldAccess access = CACHE.computeIfAbsent(
            event.getClass(),
            BaseEventFiller::introspect
        );

        access.id.set(event, UUID.randomUUID().toString());
        access.timestamp.set(event, Instant.now().toString());
        access.version.set(event, version);

        if (access.actingUserId != null && actingUserId != null) {
            access.actingUserId.set(event, actingUserId);
        }
        if (access.traceId != null && traceId != null) {
            access.traceId.set(event, traceId);
        }
    }

    private static FieldAccess introspect(Class<?> clazz) {
        return new FieldAccess(
            find(clazz, "id"),
            find(clazz, "timestamp"),
            find(clazz, "version"),
            findOptional(clazz, "actingUserId"),
            findOptional(clazz, "traceId")
        );
    }

    private static Field find(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                "Event " + clazz.getName() + " is missing required field '" + name + "'",
                e
            );
        }
    }

    private static Field findOptional(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private record FieldAccess(
        Field id,
        Field timestamp,
        Field version,
        Field actingUserId,
        Field traceId
    ) {}
}
