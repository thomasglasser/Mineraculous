package dev.thomasglasser.mineraculous.impl.mixin.minecraft.server.packs.repository;

import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatabilityHolder;
import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatibility;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Pack.Metadata.class)
public class PackMetadataMixin implements MineraculousPackCompatabilityHolder {
    @Unique
    private MineraculousPackCompatibility mineraculous$compatibility = MineraculousPackCompatibility.COMPATIBLE;

    @Override
    public MineraculousPackCompatibility mineraculous$getPackCompatibility() {
        return mineraculous$compatibility;
    }

    @Override
    public void mineraculous$setPackCompatibility(MineraculousPackCompatibility compatibility) {
        this.mineraculous$compatibility = compatibility;
    }
}
