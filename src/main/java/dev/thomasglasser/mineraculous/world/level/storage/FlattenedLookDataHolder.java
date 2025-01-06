package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Set;
import java.util.UUID;

public interface FlattenedLookDataHolder {
    Set<FlattenedLookData> mineraculous$getLookData(UUID player);

    void mineraculous$addLookData(UUID player, FlattenedLookData data);
}
