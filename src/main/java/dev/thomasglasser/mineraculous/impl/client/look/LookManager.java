package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

public class LookManager {
    private static final Map<String, MiraculousLook> LOOKS = new ConcurrentHashMap<>();
    private static final Table<UUID, Holder<Miraculous>, String> PLAYER_LOOKS = HashBasedTable.create();

    private static boolean safeMode = false;

    public static void clear() {
        LOOKS.clear();
        safeMode = false;
    }

    public static void add(MiraculousLook look) {
        if (isInSafeMode())
            return;
        LOOKS.put(look.id(), look);
    }

    public static Collection<MiraculousLook> values() {
        return isInSafeMode() ? Collections.emptySet() : LOOKS.values();
    }

    public static void assign(UUID playerId, Holder<Miraculous> miraculous, String lookId) {
        if (isInSafeMode())
            return;
        PLAYER_LOOKS.put(playerId, miraculous, lookId);
    }

    public static void unassign(UUID playerId, Holder<Miraculous> miraculous) {
        PLAYER_LOOKS.remove(playerId, miraculous);
    }

    public static @Nullable MiraculousLook getLook(UUID playerId, Holder<Miraculous> miraculous) {
        if (isInSafeMode()) {
            MineraculousConstants.LOGGER.warn("Tried to fetch look for {} in safe mode, using the default...", playerId);
            return null;
        }

        String lookId = PLAYER_LOOKS.get(playerId, miraculous);
        if (lookId == null) return null;

        return LOOKS.get(lookId);
    }

    public static boolean isInSafeMode() {
        return safeMode;
    }

    public static void enterSafeMode(String error) {
        safeMode = true;
        MineraculousConstants.LOGGER.warn("Entering safe mode due to look error: {}", error);
    }
}
