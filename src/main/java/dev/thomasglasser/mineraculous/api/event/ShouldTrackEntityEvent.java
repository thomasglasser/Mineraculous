package dev.thomasglasser.mineraculous.api.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when {@link dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData} determines if an entity should be tracked.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
 */
public class ShouldTrackEntityEvent extends EntityEvent {
    private boolean track = false;

    @ApiStatus.Internal
    public ShouldTrackEntityEvent(Entity entity) {
        super(entity);
    }

    /**
     * Returns whether the entity should be tracked.
     * 
     * @return Whether the entity should be tracked
     */
    public boolean shouldTrack() {
        return track;
    }

    /**
     * Sets whether the entity should be tracked.
     * 
     * @param track Whether the entity should be tracked
     */
    public void setShouldTrack(boolean track) {
        this.track = track;
    }
}
