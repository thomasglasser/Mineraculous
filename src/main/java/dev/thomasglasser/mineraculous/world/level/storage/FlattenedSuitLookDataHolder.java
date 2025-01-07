package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface FlattenedSuitLookDataHolder {
    Map<UUID, Set<FlattenedSuitLookData>> mineraculous$getSuitLookData();

    void mineraculous$addSuitLookData(UUID player, FlattenedSuitLookData data);
}
