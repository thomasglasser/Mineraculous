package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.impl.network.ClientboundAddRightHandParticlesPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Displays the provided {@link ParticleOptions} at the entity's humanoid right hand position.
 * 
 * @param particle The {@link ParticleOptions} to display
 */
public record RightHandParticlesAbility(ParticleOptions particle) implements Ability {
    public static final MapCodec<RightHandParticlesAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(RightHandParticlesAbility::particle)).apply(instance, RightHandParticlesAbility::new));

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context == null) {
            TommyLibServices.NETWORK.sendToTrackingClients(new ClientboundAddRightHandParticlesPayload(Optional.of(performer.getId()), particle), performer);
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.RIGHT_HAND_PARTICLES.get();
    }
}
