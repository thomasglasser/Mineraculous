package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculousTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MineraculousItemTags {
    // Kwami Foods and Treats
    public static final TagKey<Item> BUTTERFLY_KWAMI_FOODS = Miraculous.createFoodsTag(MineraculousMiraculousTypes.BUTTERFLY);
    public static final TagKey<Item> BUTTERFLY_KWAMI_TREATS = Miraculous.createTreatsTag(MineraculousMiraculousTypes.BUTTERFLY);
    public static final TagKey<Item> CAT_KWAMI_FOODS = Miraculous.createFoodsTag(MineraculousMiraculousTypes.CAT);
    public static final TagKey<Item> CAT_KWAMI_TREATS = Miraculous.createTreatsTag(MineraculousMiraculousTypes.CAT);
    public static final TagKey<Item> LADYBUG_KWAMI_FOODS = Miraculous.createFoodsTag(MineraculousMiraculousTypes.LADYBUG);
    public static final TagKey<Item> LADYBUG_KWAMI_TREATS = Miraculous.createTreatsTag(MineraculousMiraculousTypes.LADYBUG);

    // Blocks
    public static final TagKey<Item> CATACLYSM_IMMUNE = create("cataclysm_immune");

    // Cheeses
    public static final TagKey<Item> CHEESES_FOODS = createC("foods/cheeses");
    public static final TagKey<Item> CHEESES_BLOCKS_FOODS = createC("foods/cheeses_blocks");

    public static final TagKey<Item> CHEESE = create("cheese");
    public static final TagKey<Item> CHEESE_BLOCKS = create("cheese_blocks");
    public static final TagKey<Item> CAMEMBERT = create("camembert");
    public static final TagKey<Item> CAMEMBERT_BLOCKS = create("camembert_blocks");

    // Item Breaking System
    public static final TagKey<Item> TOUGH = create("tough");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, Mineraculous.modLoc(name));
    }

    private static TagKey<Item> createC(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
    }
}
