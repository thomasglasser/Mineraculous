package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundSendRightHandParticlesPayload;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

public record RightHandParticlesAbility(ParticleOptions particle, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<RightHandParticlesAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(RightHandParticlesAbility::particle),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(RightHandParticlesAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(RightHandParticlesAbility::overrideActive)).apply(instance, RightHandParticlesAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.PASSIVE) {
            TommyLibServices.NETWORK.sendToTrackingClients(new ClientboundSendRightHandParticlesPayload(entity.getId(), particle), level.getServer(), entity);
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.RIGHT_HAND_PARTICLES.get();
    }
}
