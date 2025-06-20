package dev.thomasglasser.mineraculous.impl.world.item.armortrim;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimPattern;

public class MineraculousTrimPatterns {
    public static final ResourceKey<TrimPattern> LADYBUG = create("ladybug");
    public static final ResourceKey<TrimPattern> CAT = create("cat");
    public static final ResourceKey<TrimPattern> BUTTERFLY = create("butterfly");

    private static ResourceKey<TrimPattern> create(String path) {
        return ResourceKey.create(Registries.TRIM_PATTERN, Mineraculous.modLoc(path));
    }

    public static void bootstrap(BootstrapContext<TrimPattern> context) {
        register(context, LADYBUG, MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE);
        register(context, CAT, MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE);
        register(context, BUTTERFLY, MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE);
    }

    private static void register(BootstrapContext<TrimPattern> context, ResourceKey<TrimPattern> pattern, Holder<Item> template) {
        context.register(pattern, new TrimPattern(pattern.location(), template, Component.translatable(pattern.location().toLanguageKey(pattern.registry().getPath())), false));
    }
}
