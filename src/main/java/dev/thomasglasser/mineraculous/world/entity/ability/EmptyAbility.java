package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

public record EmptyAbility(Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {
    public static final MapCodec<EmptyAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(EmptyAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(EmptyAbility::overrideActive)).apply(instance, EmptyAbility::new));

    public EmptyAbility() {
        this(Optional.empty(), false);
    }

    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        return true;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.EMPTY.get();
    }
}
