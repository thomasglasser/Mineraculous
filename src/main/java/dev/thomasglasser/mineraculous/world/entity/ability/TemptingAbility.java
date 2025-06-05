package dev.thomasglasser.mineraculous.world.entity.ability;

import net.minecraft.world.entity.LivingEntity;

public interface TemptingAbility extends Ability {
    boolean shouldTempt(LivingEntity entity);
}
