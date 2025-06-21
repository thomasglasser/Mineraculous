package dev.thomasglasser.mineraculous.impl.world.entity.decoration;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class MineraculousPaintingVariants {
    public static final ResourceKey<PaintingVariant> LADYBUG = create("ladybug");
    public static final ResourceKey<PaintingVariant> MINI_LADYBUG = create("mini_ladybug");
    public static final ResourceKey<PaintingVariant> CAT = create("cat");
    public static final ResourceKey<PaintingVariant> MINI_CAT = create("mini_cat");
    public static final ResourceKey<PaintingVariant> BUTTERFLY = create("butterfly");
    public static final ResourceKey<PaintingVariant> MINI_BUTTERFLY = create("mini_butterfly");

    private static ResourceKey<PaintingVariant> create(String name) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, Mineraculous.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<PaintingVariant> context) {
        register(context, LADYBUG, 2, 2);
        register(context, MINI_LADYBUG, 1, 1);
        register(context, CAT, 2, 2);
        register(context, MINI_CAT, 1, 1);
        register(context, BUTTERFLY, 2, 2);
        register(context, MINI_BUTTERFLY, 1, 1);
    }

    private static void register(BootstrapContext<PaintingVariant> context, ResourceKey<PaintingVariant> key, int width, int height) {
        context.register(key, new PaintingVariant(width, height, key.location()));
    }
}
