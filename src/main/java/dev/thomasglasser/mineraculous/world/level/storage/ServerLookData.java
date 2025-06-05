package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ServerLookData {
    private static final Map<UUID, Map<ResourceKey<Miraculous>, FlattenedSuitLookData>> PLAYER_SUITS = new Object2ReferenceOpenHashMap<>();
    private static final Map<UUID, Map<ResourceKey<Miraculous>, FlattenedMiraculousLookData>> PLAYER_MIRACULOUSES = new Object2ReferenceOpenHashMap<>();
    private static final Map<UUID, FlattenedKamikotizationLookData> PLAYER_KAMIKOTIZATIONS = new Object2ReferenceOpenHashMap<>();
    private static Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> commonSuits;
    private static Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> commonMiraculouses;
    private static List<Either<UUID, String>> whitelist;
    private static List<Either<UUID, String>> blacklist;

    public static Map<UUID, Map<ResourceKey<Miraculous>, FlattenedSuitLookData>> getPlayerSuits() {
        return PLAYER_SUITS;
    }

    public static Map<UUID, Map<ResourceKey<Miraculous>, FlattenedMiraculousLookData>> getPlayerMiraculouses() {
        return PLAYER_MIRACULOUSES;
    }

    public static Map<UUID, FlattenedKamikotizationLookData> getPlayerKamikotizations() {
        return PLAYER_KAMIKOTIZATIONS;
    }

    public static Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> getCommonSuits() {
        return commonSuits;
    }

    public static Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> getCommonMiraculouses() {
        return commonMiraculouses;
    }

    public static boolean isPlayerInWhitelist(Player player) {
        return checkList(whitelist, player);
    }

    public static boolean isPlayerInBlacklist(Player player) {
        return checkList(blacklist, player);
    }

    private static boolean checkList(List<Either<UUID, String>> list, Player player) {
        for (Either<UUID, String> either : list) {
            if (either.map(uuid -> uuid.equals(player.getUUID()), name -> name.equals(player.getGameProfile().getName()))) {
                return true;
            }
        }
        return false;
    }

    public static @Nullable FlattenedSuitLookData addPlayerSuit(UUID player, ResourceKey<Miraculous> miraculous, FlattenedSuitLookData data) {
        return PLAYER_SUITS.computeIfAbsent(player, p -> new Object2ReferenceOpenHashMap<>()).put(miraculous, data);
    }

    public static @Nullable FlattenedMiraculousLookData addPlayerMiraculous(UUID player, ResourceKey<Miraculous> miraculous, FlattenedMiraculousLookData data) {
        return PLAYER_MIRACULOUSES.computeIfAbsent(player, p -> new Object2ReferenceOpenHashMap<>()).put(miraculous, data);
    }

    public static void set(Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> commonSuits, Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> commonMiraculouses, List<Either<UUID, String>> whitelist, List<Either<UUID, String>> blacklist) {
        clear();
        ServerLookData.commonSuits = commonSuits;
        ServerLookData.commonMiraculouses = commonMiraculouses;
        ServerLookData.whitelist = whitelist;
        ServerLookData.blacklist = blacklist;
    }

    public static void clear() {
        PLAYER_SUITS.clear();
        PLAYER_MIRACULOUSES.clear();
        PLAYER_KAMIKOTIZATIONS.clear();
        commonSuits = null;
        commonMiraculouses = null;
        whitelist = null;
        blacklist = null;
    }
}
