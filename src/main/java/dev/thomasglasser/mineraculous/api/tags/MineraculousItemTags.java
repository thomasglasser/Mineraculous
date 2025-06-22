package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.tags.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MineraculousItemTags {
    // Kwami Foods and Treats
    /// Items that can be used to have a chance to charge the butterfly kwami.
    public static final TagKey<Item> BUTTERFLY_KWAMI_FOODS = Miraculous.createFoodsTag(Miraculouses.BUTTERFLY);
    /// Items that can be used to immediately charge the butterfly kwami.
    public static final TagKey<Item> BUTTERFLY_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.BUTTERFLY);
    /// Items that can be used to have a chance to charge the cat kwami.
    public static final TagKey<Item> CAT_KWAMI_FOODS = Miraculous.createFoodsTag(Miraculouses.CAT);
    /// Items that can be used to immediately charge the cat kwami.
    public static final TagKey<Item> CAT_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.CAT);
    /// Items that can be used to have a chance to charge the ladybug kwami.
    public static final TagKey<Item> LADYBUG_KWAMI_FOODS = Miraculous.createFoodsTag(Miraculouses.LADYBUG);
    /// Items that can be used to immediately charge the ladybug kwami.
    public static final TagKey<Item> LADYBUG_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.LADYBUG);

    // Blocks
    /// Items that are unable to be replaced by {@link Abilities#CATACLYSM}.
    public static final TagKey<Item> CATACLYSM_IMMUNE = create("cataclysm_immune");

    // Cheeses
    /// Items from any mod that can be considered cheese and food.
    public static final TagKey<Item> CHEESES_FOODS = createC("foods/cheeses");
    /// An item copy of {@link MineraculousBlockTags#CHEESE_BLOCKS_FOODS}.
    public static final TagKey<Item> CHEESE_BLOCKS_FOODS = createC("foods/cheese_blocks");

    /// Items that are normal cheese.
    public static final TagKey<Item> CHEESE = create("cheese");
    /// An item copy of {@link MineraculousBlockTags#CHEESE_BLOCKS}.
    public static final TagKey<Item> CHEESE_BLOCKS = create("cheese_blocks");
    /// Items that are normal cheese.
    public static final TagKey<Item> CAMEMBERT = create("camembert");
    /// An item copy of {@link MineraculousBlockTags#CAMEMBERT_BLOCKS}.
    public static final TagKey<Item> CAMEMBERT_BLOCKS = create("camembert_blocks");

    // Item Breaking System
    /// Items that take two tries to break if they do not have a max damage value.
    public static final TagKey<Item> TOUGH = create("tough");

    // Lucky Charms
    /// Lucky charm options when no specific pool is specified.
    public static final TagKey<Item> GENERIC_LUCKY_CHARMS = create("lucky_charms/generic");
    /// Items passed in a {@link Warden} lucky charm to distract it.
    public static final TagKey<Item> WARDEN_DISTRACTORS = create("warden_distractors");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, Mineraculous.modLoc(name));
    }

    private static TagKey<Item> createC(String name) {
        return TagUtils.createConventional(Registries.ITEM, name);
    }
}
