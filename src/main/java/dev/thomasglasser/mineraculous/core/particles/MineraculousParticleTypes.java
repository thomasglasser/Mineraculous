package dev.thomasglasser.mineraculous.core.particles;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.particle.CataclysmParticle;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class MineraculousParticleTypes
{
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Mineraculous.MOD_ID);

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CATACLYSM = PARTICLE_TYPES.register("cataclysm", () -> TommyLibServices.PARTICLE.simple("cataclysm", CataclysmParticle.Provider::new, true));

	public static void init() {}
}
