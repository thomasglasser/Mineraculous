package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface FlattenedLookDataHolder {
    Map<UUID, Set<FlattenedSuitLookData>> mineraculous$getSuitLookData();

    void mineraculous$addSuitLookData(UUID player, FlattenedSuitLookData data);

    Map<UUID, Set<FlattenedMiraculousLookData>> mineraculous$getMiraculousLookData();

    void mineraculous$addMiraculousLookData(UUID player, FlattenedMiraculousLookData data);
}
