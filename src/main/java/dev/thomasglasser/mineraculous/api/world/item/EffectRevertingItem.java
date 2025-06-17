package dev.thomasglasser.mineraculous.api.world.item;

import net.minecraft.world.entity.Entity;

public interface EffectRevertingItem {
    void revert(Entity entity);
}
