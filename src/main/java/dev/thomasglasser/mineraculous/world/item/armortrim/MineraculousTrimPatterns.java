package dev.thomasglasser.mineraculous.world.item.armortrim;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import net.minecraft.Util;
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
        register(context, LADYBUG, MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE, false);
        register(context, CAT, MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE, false);
        register(context, BUTTERFLY, MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE, false);
    }

    public static void register(BootstrapContext<TrimPattern> context, ResourceKey<TrimPattern> pattern, Holder<Item> template, boolean decal) {
        context.register(pattern, new TrimPattern(pattern.location(), template, description(pattern), decal));
    }

    public static Component description(ResourceKey<TrimPattern> pattern) {
        return Component.translatable(Util.makeDescriptionId(pattern.registry().getPath(), pattern.location()));
    }
}
