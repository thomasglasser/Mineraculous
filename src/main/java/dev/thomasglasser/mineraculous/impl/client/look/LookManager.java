package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRequestLookPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

public class LookManager {
    private static final Map<String, StoredLook> LOOKS = new ConcurrentHashMap<>();
    private static final Table<UUID, Holder<Miraculous>, String> PLAYER_LOOKS = Tables.synchronizedTable(HashBasedTable.create());

    private static boolean safeMode = false;

    public static void refresh() {
        for (String id : PLAYER_LOOKS.values()) {
            StoredLook look = LOOKS.get(id);
            if (look != null) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundRequestLookPayload(look.look().hash()));
            }
        }
        LOOKS.clear();
        safeMode = false;
    }

    public static void add(Path path, MiraculousLook look) {
        if (isInSafeMode())
            return;
        LOOKS.put(look.id(), new StoredLook(path, look));
    }

    public static ImmutableSet<MiraculousLook> values() {
        ImmutableSet.Builder<MiraculousLook> looks = ImmutableSet.builder();
        for (StoredLook look : LOOKS.values()) {
            looks.add(look.look());
        }
        return looks.build();
    }

    public static void assign(UUID playerId, Holder<Miraculous> miraculous, String lookId) {
        if (isInSafeMode())
            return;
        PLAYER_LOOKS.put(playerId, miraculous, lookId);
    }

    public static void unassign(UUID playerId, Holder<Miraculous> miraculous) {
        PLAYER_LOOKS.remove(playerId, miraculous);
    }

    public static boolean hasLook(String id, String hash) {
        return LOOKS.containsKey(id) && LOOKS.get(id).look().hash().equals(hash);
    }

    public static @Nullable MiraculousLook getLook(String id) {
        StoredLook look = LOOKS.get(id);
        return look != null ? look.look() : null;
    }

    public static @Nullable Path getPath(String id) {
        StoredLook look = LOOKS.get(id);
        return look != null ? look.path() : null;
    }

    public static @Nullable MiraculousLook getLook(UUID playerId, Holder<Miraculous> miraculous) {
        if (isInSafeMode()) {
            MineraculousConstants.LOGGER.warn("Tried to fetch look for {} in safe mode, using the default...", playerId);
            return null;
        }

        String lookId = PLAYER_LOOKS.get(playerId, miraculous);
        if (lookId == null) return null;

        return getLook(lookId);
    }

    public static boolean isInSafeMode() {
        return safeMode;
    }

    public static void enterSafeMode(String error) {
        safeMode = true;
        MineraculousConstants.LOGGER.warn("Entering safe mode due to look error: {}", error);
    }

    private record StoredLook(Path path, MiraculousLook look) {}
}
