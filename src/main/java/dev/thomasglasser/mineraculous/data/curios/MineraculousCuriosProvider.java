package dev.thomasglasser.mineraculous.data.curios;

import dev.thomasglasser.mineraculous.Mineraculous;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

public class MineraculousCuriosProvider extends CuriosDataProvider {
    public MineraculousCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(Mineraculous.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        createEntities("miraculous_holders")
                .addPlayer()
                .addSlots(
                        "brooch",
                        "ring",
                        "earring");
    }
}
