package dev.thomasglasser.mineraculous.api.world.ability.handler;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Provides special handling of {@link Ability}s.
 */
public interface AbilityHandler {
    /**
     * Triggers the advancement for performing the ability if there are any.
     *
     * @param performer The performer of the ability
     * @param context   The context of the ability
     */
    default void triggerPerformAdvancement(ServerPlayer performer, AbilityContext context) {}

    /**
     * Checks if the provided {@link ItemStack} is the provided performer's active tool
     *
     * @param stack     The stack to check for active tool
     * @param performer The performer to get the tool predicate from
     * @return Whether the provided stack is the performer's active tool
     */
    boolean isActiveTool(ItemStack stack, LivingEntity performer);
}
