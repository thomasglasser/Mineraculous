package dev.thomasglasser.mineraculous.world.entity.ability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface TemptingAbility extends Ability {
    boolean shouldTempt(ServerLevel level, Vec3 pos, Entity entity);
}
