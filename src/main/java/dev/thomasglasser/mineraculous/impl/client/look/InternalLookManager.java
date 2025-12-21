package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.ImmutableSortedSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class InternalLookManager {
    private static final Map<ResourceLocation, DefaultLook> DEFAULT_LOOKS = new Object2ObjectOpenHashMap<>();
    private static final Map<String, StoredLook> CACHED_LOOKS = new ConcurrentHashMap<>();
    private static final Map<String, StoredLook> EQUIPPABLE_LOOKS = new ConcurrentHashMap<>();

    private static boolean safeMode = false;

    public static void refresh() {
        CACHED_LOOKS.clear();
        EQUIPPABLE_LOOKS.clear();
        safeMode = false;
    }

    public static void setDefaults(Map<ResourceLocation, DefaultLook> looks) {
        DEFAULT_LOOKS.clear();
        DEFAULT_LOOKS.putAll(looks);
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

    public static DefaultLook getDefaultLook(ResourceLocation key) {
        DefaultLook look = DEFAULT_LOOKS.get(key.withPath(path -> ServerLookManager.LOOKS_SUBPATH.toString().replace("\\", "/") + "/" + path + ".json"));
        if (look == null)
            throw new RuntimeException("Default look not found: " + key);
        return look;
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
