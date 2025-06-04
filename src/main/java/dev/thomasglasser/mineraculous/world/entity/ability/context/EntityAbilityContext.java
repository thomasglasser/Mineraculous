package dev.thomasglasser.mineraculous.world.entity.ability.context;

import net.minecraft.world.entity.Entity;

public record EntityAbilityContext(Entity target) implements AbilityContext {
}
