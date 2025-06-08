package dev.thomasglasser.mineraculous.world.item;

import net.minecraft.world.entity.Entity;

public interface EffectRevertingItem {
    void revert(Entity entity);
}
