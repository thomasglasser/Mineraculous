package dev.thomasglasser.mineraculous.impl.data.looks;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.data.look.DefaultLookProvider;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.MineraculousDataGenerators;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class MineraculousDefaultLookProvider extends DefaultLookProvider {
    public MineraculousDefaultLookProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, MineraculousConstants.MOD_ID, lookupProvider);
    }

    @Override
    protected void registerLooks(HolderLookup.Provider provider) {
        HolderGetter<Miraculous> miraculouses = provider.lookupOrThrow(MineraculousRegistries.MIRACULOUS);

        miraculousLookNoAnims(miraculouses.getOrThrow(Miraculouses.LADYBUG));
        miraculousLookNoAnims(miraculouses.getOrThrow(Miraculouses.CAT));
        miraculousLookNoAnims(miraculouses.getOrThrow(Miraculouses.BUTTERFLY));

        kamikotizationLook(MineraculousDataGenerators.STORMY_KAMIKOTIZATION);
        kamikotizationLookNoAnims(MineraculousDataGenerators.CAT_KAMIKOTIZATION);
        kamikotizationLookNoAnims(MineraculousDataGenerators.LADYBUG_KAMIKOTIZATION);
    }
}
