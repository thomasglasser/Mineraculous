package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Set;
import java.util.UUID;

public interface LookDataHolder {
    Set<LookData> mineraculous$getLookData(UUID player);

    void mineraculous$addLookData(UUID player, LookData data);
}
