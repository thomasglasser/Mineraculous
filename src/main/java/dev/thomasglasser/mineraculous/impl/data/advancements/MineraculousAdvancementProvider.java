package dev.thomasglasser.mineraculous.impl.data.advancements;

import dev.thomasglasser.mineraculous.impl.data.advancements.packs.MiraculousAdvancements;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementProvider;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousAdvancementProvider extends ExtendedAdvancementProvider {
    public MineraculousAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, BiConsumer<String, String> lang, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, ReferenceOpenHashSet.of(
                new MiraculousAdvancements(lang)));
    }
}
