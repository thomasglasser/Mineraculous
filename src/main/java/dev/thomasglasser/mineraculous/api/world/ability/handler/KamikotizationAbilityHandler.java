package dev.thomasglasser.mineraculous.api.world.ability.handler;

import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * An {@link AbilityHandler} for {@link Kamikotization} abilities.
 *
 * @param kamikotization The kamikotization of the performer
 */
public record KamikotizationAbilityHandler(Holder<Kamikotization> kamikotization) implements AbilityHandler {
    @Override
    public void triggerPerformAdvancement(ServerPlayer performer, AbilityContext context) {
        MineraculousCriteriaTriggers.PERFORMED_KAMIKOTIZATION_ACTIVE_ABILITY.get().trigger(performer, kamikotization.getKey(), context.advancementContext());
    }

    @Override
    public boolean isActiveTool(ItemStack stack, LivingEntity performer) {
        return kamikotization == stack.get(MineraculousDataComponents.KAMIKOTIZATION) && Active.isActive(stack, true);
    }
}
