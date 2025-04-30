package dev.thomasglasser.mineraculous.data.advancements;

import dev.thomasglasser.mineraculous.data.advancements.packs.MineraculousMiraculousAdvancements;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementProvider;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MineraculousAdvancementProvider extends ExtendedAdvancementProvider {
    public MineraculousAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper, LanguageProvider enUs) {
        super(output, registries, existingFileHelper, Set.of(
                new MineraculousMiraculousAdvancements(enUs)));
    }
}
