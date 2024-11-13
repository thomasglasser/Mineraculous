package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record RightHandParticlesAbility(ParticleOptions particle) implements Ability {
    public static final MapCodec<RightHandParticlesAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(RightHandParticlesAbility::particle)).apply(instance, RightHandParticlesAbility::new));

    @Override
    public boolean perform(AbilityData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.PASSIVE && level.isClientSide) {
            double randomShiftForward = 1.0 / level.random.nextInt(8, 15);
            double randomShiftRight = 1.0 / level.random.nextInt(8, 15);
            double randomShiftUp = 1.0 / level.random.nextInt(15, 50);
            if (level.random.nextBoolean())
                randomShiftForward = -randomShiftForward;
            if (level.random.nextBoolean())
                randomShiftRight = -randomShiftRight;

            if (MineraculousClientUtils.isFirstPerson()) {
                MineraculousClientUtils.renderParticlesFollowingEntity(performer, particle, 0.3, 0.1 + randomShiftForward, 0.23 + randomShiftRight, -0.1 - randomShiftUp, 0.4F, true);
            } else {
                MineraculousClientUtils.renderParticlesFollowingEntity(performer, particle, 0, 0.1 + randomShiftForward, 0.35 + randomShiftRight, 0.7 + randomShiftUp, 0.6F, false);
            }

            return true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.RIGHT_HAND_PARTICLES.get();
    }
}
