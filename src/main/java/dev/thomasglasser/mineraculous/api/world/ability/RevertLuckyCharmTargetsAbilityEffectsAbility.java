package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.EffectRevertingItem;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.api.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
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
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context == null && data.powerActive() && performer instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getMainHandItem();
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                UUID performerId = data.power().map(miraculous -> {
                    UUID kwamiId = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).kwamiData().map(KwamiData::uuid).orElse(null);
                    KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                    if (kwamiData != null && kwamiData.uuid().equals(kwamiId))
                        return kwamiId;
                    return null;
                }, kamikotization -> {
                    if (kamikotization == stack.get(MineraculousDataComponents.KAMIKOTIZATION)) {
                        return livingEntity.getUUID();
                    }
                    return null;
                });
                if (luckyCharm.owner().equals(performerId)) {
                    luckyCharm.target().ifPresent(target -> {
                        AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
                        for (UUID relatedId : entityData.getAndClearTrackedAndRelatedEntities(target)) {
                            Entity related = level.getEntity(relatedId);
                            if (related != null) {
                                related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotization -> {
                                    Kamikotization value = kamikotization.value();
                                    AbilityData abilityData = new AbilityData(0, Either.right(kamikotization), false);
                                    value.powerSource().ifLeft(tool -> {
                                        if (tool.getItem() instanceof EffectRevertingItem item) {
                                            item.revert(related);
                                        }
                                    }).ifRight(ability -> ability.value().revert(abilityData, level, related));
                                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related));
                                    AbilityReversionItemData.get(level).revertKamikotized(relatedId, level);
                                });
                                MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                                for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
                                    Miraculous value = miraculous.value();
                                    AbilityData abilityData = new AbilityData(miraculousesData.get(miraculous).powerLevel(), Either.left(miraculous), false);
                                    value.activeAbility().value().revert(abilityData, level, related);
                                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related));
                                }
                            }
                        }
                    });
                    LuckyCharmIdData.get(level).incrementLuckyCharmId(performerId);
                    Ability.playSound(level, performer, revertSound);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.REVERT_LUCKY_CHARM_TARGETS_ABILITY_EFFECTS.get();
    }
}
