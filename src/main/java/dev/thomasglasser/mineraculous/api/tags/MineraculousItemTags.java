package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.tommylib.api.tags.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MineraculousItemTags {
    // Kwami Foods and Treats
    /// Items that can be used to have a better chance to charge the butterfly kwami.
    public static final TagKey<Item> BUTTERFLY_KWAMI_PREFERRED_FOODS = Miraculous.createPreferredFoodsTag(Miraculouses.BUTTERFLY);
    /// Items that can be used to immediately charge the butterfly kwami.
    public static final TagKey<Item> BUTTERFLY_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.BUTTERFLY);
    /// Items that can be used to have a better chance to charge the cat kwami.
    public static final TagKey<Item> CAT_KWAMI_PREFERRED_FOODS = Miraculous.createPreferredFoodsTag(Miraculouses.CAT);
    /// Items that can be used to immediately charge the cat kwami.
    public static final TagKey<Item> CAT_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.CAT);
    /// Items that can be used to have a better chance to charge the ladybug kwami.
    public static final TagKey<Item> LADYBUG_KWAMI_PREFERRED_FOODS = Miraculous.createPreferredFoodsTag(Miraculouses.LADYBUG);
    /// Items that can be used to immediately charge the ladybug kwami.
    public static final TagKey<Item> LADYBUG_KWAMI_TREATS = Miraculous.createTreatsTag(Miraculouses.LADYBUG);

    // Cheeses
    /// Items from any mod that can be considered cheese and food.
    public static final TagKey<Item> CHEESES_FOODS = createC("foods/cheeses");
    /// An item copy of {@link MineraculousBlockTags#CHEESE_BLOCKS_FOODS}.
    public static final TagKey<Item> CHEESE_BLOCKS_FOODS = createC("foods/cheese_blocks");

    /// Items that are normal cheese.
    public static final TagKey<Item> CHEESE = create("cheese");
    /// An item copy of {@link MineraculousBlockTags#CHEESE_BLOCKS}.
    public static final TagKey<Item> CHEESE_BLOCKS = create("cheese_blocks");
    /// Items that are camembert cheese.
    public static final TagKey<Item> CAMEMBERT = create("camembert");
    /// An item copy of {@link MineraculousBlockTags#CAMEMBERT_BLOCKS}.
    public static final TagKey<Item> CAMEMBERT_BLOCKS = create("camembert_blocks");

    // Item Breaking System
    /// Items that are unable to be replaced by {@link dev.thomasglasser.mineraculous.api.world.ability.Abilities#CATACLYSM}.
    public static final TagKey<Item> CATACLYSM_IMMUNE = create("cataclysm_immune");
    /// Items that take two tries to break if they do not have a max damage value.
    public static final TagKey<Item> TOUGH = create("tough");

    // Lucky Charms
    /// Items that do not have a visual change when given the {@link dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents#LUCKY_CHARM} component.
    public static final TagKey<Item> LUCKY_CHARM_SHADER_IMMUNE = create("lucky_charm_shader_immune");
    /// {@link net.minecraft.world.item.ProjectileItem}s that shoot down from the {@link dev.thomasglasser.mineraculous.impl.world.entity.LuckyCharmItemSpawner} instead of dropping normally
    public static final TagKey<Item> SHOOTING_PROJECTILES = create("shooting_projectiles");
    /// Lucky charm options when no specific pool is selected.
    public static final TagKey<Item> GENERIC_LUCKY_CHARMS = create("lucky_charms/generic");
    /// Items passed in a {@link net.minecraft.world.entity.monster.warden.Warden} lucky charm to distract it.
    public static final TagKey<Item> WARDEN_DISTRACTORS = create("warden_distractors");

    // Kamikotization
    /// Items that cannot be used for {@link dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization}.
    public static final TagKey<Item> KAMIKOTIZATION_IMMUNE = create("kamikotization_immune");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, MineraculousConstants.modLoc(name));
    }

    private static TagKey<Item> createC(String name) {
        return TagUtils.createConventional(Registries.ITEM, name);
    }
}
