package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface FlattenedLookDataHolder {
    Map<UUID, Set<FlattenedLookData>> mineraculous$getLookData();

    Set<FlattenedLookData> mineraculous$getLookData(UUID player);

    void mineraculous$addLookData(UUID player, FlattenedLookData data);
}
