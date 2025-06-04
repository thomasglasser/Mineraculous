package dev.thomasglasser.mineraculous.world.entity.ability;

import net.minecraft.world.entity.LivingEntity;

public interface TemptingAbility {
    boolean shouldTempt(LivingEntity entity);
}
