package dev.thomasglasser.mineraculous.world.item.armortrim;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.armortrim.TrimPattern;

public class MineraculousTrimPatterns {
    public static final ResourceKey<TrimPattern> LADYBUG = create("ladybug");
    public static final ResourceKey<TrimPattern> CAT = create("cat");
    public static final ResourceKey<TrimPattern> BUTTERFLY = create("butterfly");

    private static ResourceKey<TrimPattern> create(String path) {
        return ResourceKey.create(Registries.TRIM_PATTERN, Mineraculous.modLoc(path));
    }
}
