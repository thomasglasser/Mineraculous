package dev.thomasglasser.mineraculous.impl.data.particles;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class MineraculousParticleDescriptionProvider extends ParticleDescriptionProvider {
    public MineraculousParticleDescriptionProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper);
    }

    @Override
    protected void addDescriptions() {
        sprite(MineraculousParticleTypes.BLACK_ORB.get(), Mineraculous.modLoc("black_orb"));
        sprite(MineraculousParticleTypes.KAMIKOTIZATION.get(), Mineraculous.modLoc("kamikotization"));
        sprite(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), Mineraculous.modLoc("ladybug"));
    }
}
