package dev.thomasglasser.mineraculous.api.world.ability.handler;

import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * An {@link AbilityHandler} for {@link Miraculous} abilities.
 *
 * @param miraculous The miraculous of the performer
 */
public record MiraculousAbilityHandler(Holder<Miraculous> miraculous) implements AbilityHandler {
    @Override
    public void triggerPerformAdvancement(ServerPlayer performer, AbilityContext context) {
        MineraculousCriteriaTriggers.PERFORMED_MIRACULOUS_ACTIVE_ABILITY.get().trigger(performer, miraculous.getKey(), context.advancementContext());
    }

    @Override
    public @Nullable UUID getAndAssignBlame(ItemStack stack, LivingEntity performer) {
        UUID id = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).curiosData().map(curiosData -> CuriosUtils.getStackInSlot(performer, curiosData).get(MineraculousDataComponents.MIRACULOUS_ID)).orElse(null);
        stack.set(MineraculousDataComponents.MIRACULOUS_ID, id);
        return id;
    }

    @Override
    public @Nullable UUID getMatchingBlame(ItemStack stack, LivingEntity performer) {
        UUID performerId = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).curiosData().map(curiosData -> CuriosUtils.getStackInSlot(performer, curiosData).get(MineraculousDataComponents.MIRACULOUS_ID)).orElse(null);
        UUID stackId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
        if (performerId != null && performerId.equals(stackId))
            return performerId;
        return null;
    }

    @Override
    public boolean isActiveTool(ItemStack stack, LivingEntity performer) {
        UUID stackId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
        return miraculous.value().tool().is(stack.getItem()) && stackId != null && performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).curiosData().map(curiosData -> CuriosUtils.getStackInSlot(performer, curiosData).get(MineraculousDataComponents.MIRACULOUS_ID)).map(id -> id.equals(stackId)).orElse(false) && stack.getOrDefault(MineraculousDataComponents.ACTIVE, true);
    }
}
