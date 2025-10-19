package dev.thomasglasser.mineraculous.api.world.ability.handler;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
     * Determines the blame {@link UUID} for the provided performer.
     * 
     * @param performer The performer to get the blame from
     * @return The blame UUID, or null if there isn't one
     */
    @Nullable
    UUID getBlame(LivingEntity performer);

    /**
     * Determines the blame {@link UUID} for the performer and assigns it to the provided {@link ItemStack}.
     *
     * @param blame     The blame UUID to assign
     * @param stack     The stack to assign the blame to
     * @param performer The performer to get the blame from
     */
    void assignBlame(UUID blame, ItemStack stack, LivingEntity performer);

    /**
     * Determines the blame {@link UUID} and returns it if it matches the blame of the provided {@link ItemStack}.
     *
     * @param stack     The stack to check the blame against
     * @param performer The performer to get the blame from
     * @return The blame UUID if it matches, or null
     */
    @Nullable
    UUID getMatchingBlame(ItemStack stack, LivingEntity performer);

    /**
     * Checks if the provided {@link ItemStack} is the provided performer's active tool
     *
     * @param stack     The stack to check for active tool
     * @param performer The performer to get the tool predicate from
     * @return Whether the provided stack is the performer's active tool
     */
    boolean isActiveTool(ItemStack stack, LivingEntity performer);
}
