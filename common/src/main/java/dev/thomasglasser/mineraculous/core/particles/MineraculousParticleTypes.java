package dev.thomasglasser.mineraculous.core.particles;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.particle.CataclysmParticle;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class MineraculousParticleTypes
{
	public static final RegistrationProvider<ParticleType<?>> PARTICLE_TYPES = RegistrationProvider.get(Registries.PARTICLE_TYPE, Mineraculous.MOD_ID);

	public static final RegistryObject<SimpleParticleType> CATACLYSM = PARTICLE_TYPES.register("cataclysm", () -> TommyLibServices.PARTICLE.simple("cataclysm", CataclysmParticle.Provider::new, true));

	public static void init() {}
}
