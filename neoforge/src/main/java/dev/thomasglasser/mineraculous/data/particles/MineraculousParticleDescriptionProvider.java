package dev.thomasglasser.mineraculous.data.particles;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class MineraculousParticleDescriptionProvider extends ParticleDescriptionProvider
{
	/**
	 * Creates an instance of the data provider.
	 *
	 * @param output     the expected root directory the data generator outputs to
	 * @param fileHelper the helper used to validate a texture's existence
	 */
	public MineraculousParticleDescriptionProvider(PackOutput output, ExistingFileHelper fileHelper)
	{
		super(output, fileHelper);
	}

	@Override
	protected void addDescriptions()
	{
		sprite(MineraculousParticleTypes.CATACLYSM.get(), Mineraculous.modLoc("cataclysm"));
	}
}
