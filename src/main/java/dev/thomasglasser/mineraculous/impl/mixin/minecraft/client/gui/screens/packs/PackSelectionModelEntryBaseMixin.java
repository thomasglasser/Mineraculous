package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.gui.screens.packs;

import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatabilityHolder;
import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatibility;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackSelectionModel.EntryBase.class)
public class PackSelectionModelEntryBaseMixin implements MineraculousPackCompatabilityHolder {
    @Unique
    private MineraculousPackCompatibility mineraculous$compatibility = MineraculousPackCompatibility.COMPATIBLE;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(PackSelectionModel this$0, Pack pack, CallbackInfo ci) {
        mineraculous$setPackCompatibility(((MineraculousPackCompatabilityHolder) (Object) pack.metadata).mineraculous$getPackCompatibility());
    }

    @Override
    public MineraculousPackCompatibility mineraculous$getPackCompatibility() {
        return mineraculous$compatibility;
    }

    @Override
    public void mineraculous$setPackCompatibility(MineraculousPackCompatibility compatibility) {
        this.mineraculous$compatibility = compatibility;
    }
}
