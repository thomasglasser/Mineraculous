package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.ImmutableSortedSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public class InternalLookManager {
    private static final Map<String, StoredLook> CACHED_LOOKS = new ConcurrentHashMap<>();
    private static final Map<String, StoredLook> EQUIPPABLE_LOOKS = new ConcurrentHashMap<>();

    private static boolean safeMode = false;

    public static void refresh() {
        CACHED_LOOKS.clear();
        EQUIPPABLE_LOOKS.clear();
        safeMode = false;
    }

    public static void add(Path path, Look look, boolean equippable) {
        if (isInSafeMode())
            return;
        StoredLook stored = new StoredLook(path, look);
        CACHED_LOOKS.put(look.hash(), stored);
        if (equippable)
            EQUIPPABLE_LOOKS.put(look.hash(), stored);
    }

    public static ImmutableSortedSet<Look> getEquippable() {
        ImmutableSortedSet.Builder<Look> looks = ImmutableSortedSet.orderedBy(Comparator.comparing(Look::name));
        for (StoredLook look : EQUIPPABLE_LOOKS.values()) {
            looks.add(look.look());
        }
        return looks.build();
    }

    public static @Nullable Look getCachedLook(String hash) {
        StoredLook look = CACHED_LOOKS.get(hash);
        return look != null ? look.look() : null;
    }

    public static @Nullable Look getEquippableLook(String hash) {
        StoredLook look = EQUIPPABLE_LOOKS.get(hash);
        return look != null ? look.look() : null;
    }

    public static @Nullable Path getEquippablePath(String hash) {
        StoredLook look = EQUIPPABLE_LOOKS.get(hash);
        return look != null ? look.path() : null;
    }

    public static boolean isInSafeMode() {
        return safeMode;
    }

    public static void enterSafeMode(String error) {
        safeMode = true;
        MineraculousConstants.LOGGER.warn("Entering safe mode due to look error: {}", error);
    }

    private record StoredLook(Path path, Look look) {}
}
