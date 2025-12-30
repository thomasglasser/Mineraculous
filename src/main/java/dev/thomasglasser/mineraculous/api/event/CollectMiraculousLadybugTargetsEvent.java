package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetCollector;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when collecting targets for the Miraculous Ladybug ability.
 *
 * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
 */
public class CollectMiraculousLadybugTargetsEvent extends Event {
    private final ServerLevel level;
    private final UUID performerId;
    private final UUID targetId;
    private final MiraculousLadybugTargetCollector targetCollector;

    @ApiStatus.Internal
    public CollectMiraculousLadybugTargetsEvent(ServerLevel level, UUID performerId, UUID targetId, MiraculousLadybugTargetCollector targetCollector) {
        this.level = level;
        this.performerId = performerId;
        this.targetId = targetId;
        this.targetCollector = targetCollector;
    }

    /**
     * Returns the level where the targets are being collected.
     * 
     * @return The level where the targets are being collected
     */
    public ServerLevel getLevel() {
        return level;
    }

    /**
     * Returns the UUID of the performer of the ability.
     * 
     * @return The UUID of the performer of the ability
     */
    public UUID getPerformerId() {
        return performerId;
    }

    /**
     * Returns the UUID of the target of the ability.
     * 
     * @return The UUID of the target of the ability
     */
    public UUID getTargetId() {
        return targetId;
    }

    /**
     * Returns the target collector.
     * 
     * @return The target collector
     */
    public MiraculousLadybugTargetCollector getTargetCollector() {
        return targetCollector;
    }
}
