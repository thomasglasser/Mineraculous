package dev.thomasglasser.mineraculous.api.world.ability.handler;

import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiData;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
    public @Nullable UUID getAndAssignBlame(ItemStack stack, Entity performer) {
        KwamiData kwamiData = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).kwamiData().orElse(null);
        stack.set(MineraculousDataComponents.KWAMI_DATA, kwamiData);
        return kwamiData != null ? kwamiData.uuid() : null;
    }

    @Override
    public @Nullable UUID getMatchingBlame(ItemStack stack, Entity performer) {
        UUID kwamiId = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).kwamiData().map(KwamiData::uuid).orElse(null);
        KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
        if (kwamiData != null && kwamiData.uuid().equals(kwamiId))
            return kwamiId;
        return null;
    }

    @Override
    public boolean isActiveTool(ItemStack stack, Entity performer) {
        KwamiData stackKwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
        return miraculous.value().tool().is(stack.getItem()) && stackKwamiData != null && performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).kwamiData().map(kwamiData -> kwamiData.uuid().equals(stackKwamiData.uuid())).orElse(false) && stack.getOrDefault(MineraculousDataComponents.ACTIVE, true);
    }
}
