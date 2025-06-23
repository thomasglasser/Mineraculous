package dev.thomasglasser.mineraculous.api.world.ability.context;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Used in entity-related ability behavior.
 * 
 * @param target The entity the ability was performed on
 */
public record EntityAbilityContext(Entity target) implements AbilityContext {
    public static final String ADVANCEMENT_CONTEXT = "entity";
    public static final String ADVANCEMENT_CONTEXT_LIVING = "living_entity";

    @Override
    public String advancementContext() {
        return target instanceof LivingEntity ? ADVANCEMENT_CONTEXT_LIVING : ADVANCEMENT_CONTEXT;
    }
}
