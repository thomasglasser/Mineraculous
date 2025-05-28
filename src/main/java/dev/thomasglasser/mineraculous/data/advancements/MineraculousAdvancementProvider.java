package dev.thomasglasser.mineraculous.data.advancements;

import dev.thomasglasser.mineraculous.data.advancements.packs.MineraculousMiraculousAdvancements;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementProvider;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousAdvancementProvider extends ExtendedAdvancementProvider {
    public MineraculousAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper, BiConsumer<String, String> lang) {
        super(output, registries, existingFileHelper, ReferenceOpenHashSet.of(
                new MineraculousMiraculousAdvancements(lang)));
    }
}
