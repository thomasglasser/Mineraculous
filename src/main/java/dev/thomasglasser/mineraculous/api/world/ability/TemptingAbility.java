package dev.thomasglasser.mineraculous.api.world.ability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * An {@link Ability} that is able to tempt an entity.
 * This only applies if the entity explicitly uses it.
 */
public interface TemptingAbility extends Ability {
    /**
     * Checks if the ability should tempt the provided entity to the performer.
     * @param level The level the performer and entity are in
     * @param performer The performer of the ability
     * @param entity The entity that could be tempted
     * @return Whether the entity should be tempted to the performer
     */
    boolean shouldTempt(ServerLevel level, Entity performer, Entity entity);
}
