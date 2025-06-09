package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.EffectRevertingItem;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharm;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record RevertLuckyCharmTargetsAbilityEffectsAbility() implements Ability {
    public static final RevertLuckyCharmTargetsAbilityEffectsAbility INSTANCE = new RevertLuckyCharmTargetsAbilityEffectsAbility();
    public static final MapCodec<RevertLuckyCharmTargetsAbilityEffectsAbility> CODEC = MapCodec.unit(INSTANCE);

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
