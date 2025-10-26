package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.EffectRevertingItem;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LuckyCharmIdData;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Reverts the ability effects of the {@link LuckyCharm} target and related entities.
 *
 * @param revertSound The sound to play when reverting ability effects
 */
public record RevertLuckyCharmTargetsAbilityEffectsAbility(Optional<Holder<SoundEvent>> revertSound) implements Ability {
    public static final MapCodec<RevertLuckyCharmTargetsAbilityEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(RevertLuckyCharmTargetsAbilityEffectsAbility::revertSound)).apply(instance, RevertLuckyCharmTargetsAbilityEffectsAbility::new));

    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context == null && data.powerActive()) {
            ItemStack stack = performer.getMainHandItem();
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                UUID performerId = handler.getMatchingBlame(stack, performer);
                if (luckyCharm.owner().equals(performerId)) {
                    luckyCharm.target().ifPresent(target -> {
                        AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
                        for (UUID relatedId : entityData.getAndClearTrackedAndRelatedEntities(target)) {
                            Entity r = level.getEntity(relatedId);
                            if (r instanceof LivingEntity related) {
                                related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotizationData -> {
                                    Kamikotization value = kamikotizationData.kamikotization().value();
                                    AbilityData abilityData = AbilityData.of(kamikotizationData);
                                    value.powerSource().ifLeft(tool -> {
                                        if (tool.getItem() instanceof EffectRevertingItem item) {
                                            item.revert(related);
                                        }
                                    }).ifRight(ability -> ability.value().revert(abilityData, level, related));
                                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related));
                                });
                                MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                                for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
                                    Miraculous value = miraculous.value();
                                    AbilityData abilityData = AbilityData.of(miraculousesData.get(miraculous));
                                    value.activeAbility().value().revert(abilityData, level, related);
                                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related));
                                }
                            }
                        }
                    });
                    LuckyCharmIdData.get(level).incrementLuckyCharmId(performerId);
                    Ability.playSound(level, performer, revertSound);
                    return State.CONSUME;
                }
            }
        }
        return State.PASS;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.REVERT_LUCKY_CHARM_TARGETS_ABILITY_EFFECTS.get();
    }
}
