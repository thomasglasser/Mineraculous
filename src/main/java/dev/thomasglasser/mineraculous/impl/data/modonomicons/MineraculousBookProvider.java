package dev.thomasglasser.mineraculous.impl.data.modonomicons;

import com.google.common.collect.ImmutableList;
import com.klikli_dev.modonomicon.api.datagen.BookProvider;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class MineraculousBookProvider extends BookProvider {
    public MineraculousBookProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, BiConsumer<String, String> lang) {
        super(packOutput, registries, MineraculousConstants.MOD_ID, ImmutableList.of(
                new WikiBookSubProvider(lang)));
    }
}
