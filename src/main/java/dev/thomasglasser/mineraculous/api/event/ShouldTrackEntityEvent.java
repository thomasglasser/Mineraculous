package dev.thomasglasser.mineraculous.api.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class ShouldTrackEntityEvent extends EntityEvent {
    private boolean track = false;

    public ShouldTrackEntityEvent(Entity entity) {
        super(entity);
    }

    public boolean shouldTrack() {
        return track;
    }

    public void setShouldTrack(boolean track) {
        this.track = track;
    }
}
