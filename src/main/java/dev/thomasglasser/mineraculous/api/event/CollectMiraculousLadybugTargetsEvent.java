package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetCollector;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class CollectMiraculousLadybugTargetsEvent extends Event {
    private final ServerLevel level;
    private final UUID performerId;
    private final UUID targetId;
    private final MiraculousLadybugTargetCollector targetCollector;

    public CollectMiraculousLadybugTargetsEvent(ServerLevel level, UUID performerId, UUID targetId, MiraculousLadybugTargetCollector targetCollector) {
        this.level = level;
        this.performerId = performerId;
        this.targetId = targetId;
        this.targetCollector = targetCollector;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public UUID getPerformerId() {
        return performerId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public MiraculousLadybugTargetCollector getTargetCollector() {
        return targetCollector;
    }
}
