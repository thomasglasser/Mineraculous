package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public interface FlattenedLookDataHolder {
    Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> mineraculous$getCommonSuitLookData();

    void mineraculous$setCommonSuitLookData(Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> data);

    Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> mineraculous$getCommonMiraculousLookData();

    void mineraculous$setCommonMiraculousLookData(Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> data);

    Map<UUID, Set<FlattenedSuitLookData>> mineraculous$getSuitLookData();

    void mineraculous$addSuitLookData(UUID player, FlattenedSuitLookData data);

    Map<UUID, Set<FlattenedMiraculousLookData>> mineraculous$getMiraculousLookData();

    void mineraculous$addMiraculousLookData(UUID player, FlattenedMiraculousLookData data);

    Map<UUID, FlattenedKamikotizationLookData> mineraculous$getKamikotizationLookData();

    void mineraculous$addKamikotizationLookData(UUID player, FlattenedKamikotizationLookData data);

    boolean mineraculous$isPlayerWhitelisted(Player player);

    void mineraculous$setWhitelist(List<Either<UUID, String>> whitelist);

    boolean mineraculous$isPlayerBlacklisted(Player player);

    void mineraculous$setBlacklist(List<Either<UUID, String>> blacklist);
}
