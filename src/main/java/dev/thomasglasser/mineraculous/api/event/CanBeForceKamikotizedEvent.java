package dev.thomasglasser.mineraculous.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired to determine if an entity can be force kamikotized.
 *
 * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
 */
public class CanBeForceKamikotizedEvent extends LivingEvent {
    private boolean canBeKamikotized = true;

    @ApiStatus.Internal
    public CanBeForceKamikotizedEvent(LivingEntity entity) {
        super(entity);
    }

    /**
     * Returns whether the entity can be force kamikotized.
     * 
     * @return Whether the entity can be force kamikotized
     */
    public boolean canBeKamikotized() {
        return canBeKamikotized;
    }

    /**
     * Sets whether the entity can be force kamikotized.
     * 
     * @param canBeKamikotized Whether the entity can be force kamikotized
     */
    public void setCanBeKamikotized(boolean canBeKamikotized) {
        this.canBeKamikotized = canBeKamikotized;
    }
}
