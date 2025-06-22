package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Applies passive infinite effects while transformed.
 *
 * @param effects        The effects to apply
 * @param startAmplifier The amplifier for the effect before considering {@link AbilityData#powerLevel()}
 */
public record PassiveEffectsAbility(HolderSet<MobEffect> effects, int startAmplifier) implements Ability {
    public static final MapCodec<PassiveEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.MOB_EFFECT, MobEffect.CODEC, false).fieldOf("effects").forGetter(PassiveEffectsAbility::effects),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("start_amplifier", 0).forGetter(PassiveEffectsAbility::startAmplifier)).apply(instance, PassiveEffectsAbility::new));

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context == null && performer instanceof LivingEntity livingEntity) {
            for (Holder<MobEffect> effect : effects) {
                if (!livingEntity.hasEffect(effect)) {
                    MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, effect, startAmplifier + (data.powerLevel() / 10));
                }
            }
        }
        return false;
    }

    @Override
    public void transform(AbilityData data, ServerLevel level, Entity performer) {
        if (effects.size() > 0 && performer instanceof LivingEntity livingEntity) {
            effects.forEach(effect -> MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, effect, startAmplifier + (data.powerLevel() / 10)));
        }
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, Entity performer) {
        if (effects.size() > 0 && performer instanceof LivingEntity livingEntity) {
            effects.forEach(livingEntity::removeEffect);
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.PASSIVE_EFFECTS.get();
    }
}
