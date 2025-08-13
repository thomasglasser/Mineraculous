package dev.thomasglasser.mineraculous.api.core.particles;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousParticleTypes {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLACK_ORB = PARTICLE_TYPES.register("black_orb", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> KAMIKOTIZATION = PARTICLE_TYPES.register("kamikotization", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SUMMONING_LADYBUG = PARTICLE_TYPES.register("summoning_ladybug", () -> new SimpleParticleType(true));

    @ApiStatus.Internal
    public static void init() {}
}
