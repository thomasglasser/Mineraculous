package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.tags.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MineraculousItemTags {
    // Kwami Foods and Treats
    public static final TagKey<Item> BUTTERFLY_KWAMI_FOODS = Miraculous.createFoodsTag(Miraculouses.BUTTERFLY);
    public static final TagKey<Item> BUTTERFLY_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.BUTTERFLY);
    public static final TagKey<Item> CAT_KWAMI_FOODS = Miraculous.createFoodsTag(Miraculouses.CAT);
    public static final TagKey<Item> CAT_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.CAT);
    public static final TagKey<Item> LADYBUG_KWAMI_FOODS = Miraculous.createFoodsTag(Miraculouses.LADYBUG);
    public static final TagKey<Item> LADYBUG_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.LADYBUG);

    // Blocks
    public static final TagKey<Item> CATACLYSM_IMMUNE = create("cataclysm_immune");

    // Cheeses
    public static final TagKey<Item> CHEESES_FOODS = createC("foods/cheeses");
    public static final TagKey<Item> CHEESE_BLOCKS_FOODS = createC("foods/cheese_blocks");

    public static final TagKey<Item> CHEESE = create("cheese");
    public static final TagKey<Item> CHEESE_BLOCKS = create("cheese_blocks");
    public static final TagKey<Item> CAMEMBERT = create("camembert");
    public static final TagKey<Item> CAMEMBERT_BLOCKS = create("camembert_blocks");

    // Item Breaking System
    public static final TagKey<Item> TOUGH = create("tough");

    // Lucky Charms
    public static final TagKey<Item> GENERIC_LUCKY_CHARMS = create("lucky_charms/generic");
    public static final TagKey<Item> WARDEN_DISTRACTORS = create("warden_distractors");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, Mineraculous.modLoc(name));
    }

    private static TagKey<Item> createC(String name) {
        return TagUtils.createConventional(Registries.ITEM, name);
    }
}
