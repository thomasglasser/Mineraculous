package dev.thomasglasser.mineraculous.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class CanBeForceKamikotizedEvent extends LivingEvent {
    private boolean canBeKamikotized = true;

    public CanBeForceKamikotizedEvent(LivingEntity entity) {
        super(entity);
    }

    public boolean canBeKamikotized() {
        return canBeKamikotized;
    }

    public void setCanBeKamikotized(boolean canBeKamikotized) {
        this.canBeKamikotized = canBeKamikotized;
    }
}
