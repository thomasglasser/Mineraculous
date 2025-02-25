package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public record ApplyEffectsWhileTransformedAbility(HolderSet<MobEffect> effects, int startLevel, Optional<Holder<SoundEvent>> startSound) implements Ability {

    public static final MapCodec<ApplyEffectsWhileTransformedAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.MOB_EFFECT, MobEffect.CODEC, false).fieldOf("effects").forGetter(ApplyEffectsWhileTransformedAbility::effects),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("start_level", 1).forGetter(ApplyEffectsWhileTransformedAbility::startLevel),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(ApplyEffectsWhileTransformedAbility::startSound)).apply(instance, ApplyEffectsWhileTransformedAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        return false;
    }

    @Override
    public void transform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (effects.size() > 0) {
            effects.forEach(effect -> entity.addEffect(MineraculousEntityEvents.INFINITE_HIDDEN_EFFECT.apply(effect, startLevel + (data.powerLevel() / 10))));
            playStartSound(level, pos);
        }
    }

    public void apply(LivingEntity entity, int powerLevel) {
        effects.forEach(effect -> entity.addEffect(MineraculousEntityEvents.INFINITE_HIDDEN_EFFECT.apply(effect, startLevel + (powerLevel / 10))));
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (effects.size() > 0) {
            effects.forEach(entity::removeEffect);
        }
    }

    @Override
    public boolean overrideActive() {
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.APPLY_EFFECTS_WHILE_TRANSFORMED.get();
    }
}
