package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedItemTagsProvider;
import dev.thomasglasser.tommylib.api.tags.ConventionalItemTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousItemTagsProvider extends ExtendedItemTagsProvider {
    public MineraculousItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockLookup, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockLookup, MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addKwamiFoods();
        addFoods();
        addArmors();
        addArmorTrims();
        addTrees();
        addCurios();
        addAbilities();

        // Misc
        tag(ItemTags.SMALL_FLOWERS)
                .add(MineraculousBlocks.HIBISCUS_BUSH.asItem());

        tag(MineraculousItemTags.KAMIKOTIZATION_IMMUNE)
                .add(MineraculousItems.KWAMI);
        tag(ItemTags.CHICKEN_FOOD)
                .add(MineraculousItems.ALMOND);
        tag(ItemTags.PARROT_FOOD)
                .add(MineraculousItems.ALMOND);

        tag(MineraculousItemTags.TOUGH)
                .add(Items.NETHER_STAR)
                .add(Items.TOTEM_OF_UNDYING)
                .add(Items.MINECART)
                .add(Items.CHEST_MINECART)
                .add(Items.FURNACE_MINECART)
                .add(Items.TNT_MINECART)
                .add(Items.HOPPER_MINECART)
                .addTag(ItemTags.BOATS)
                .addTag(ItemTags.COALS)
                .addTag(ConventionalItemTags.BRICKS)
                .addTag(ConventionalItemTags.GEMS)
                .addTag(ConventionalItemTags.INGOTS)
                .addTag(ConventionalItemTags.RAW_MATERIALS)
                .addTag(ConventionalItemTags.ORES)
                .addTag(ConventionalItemTags.BUCKETS);
    }

    protected void curios(String slot, Item... items) {
        IntrinsicTagAppender<Item> curios = tag(TagKey.create(Registries.ITEM, MineraculousConstants.Dependencies.CURIOS.modLoc(slot)));

        for (Item item : items) {
            curios.add(item);
        }
    }

    protected void curios(Item item, String... slots) {
        for (String slot : slots) {
            IntrinsicTagAppender<Item> curios = tag(TagKey.create(Registries.ITEM, MineraculousConstants.Dependencies.CURIOS.modLoc(slot)));
            curios.add(item);
        }
    }

    private void addTrees() {
        // Almond
        woodSet(MineraculousBlocks.ALMOND_WOOD_SET);
        leavesSet(MineraculousBlocks.ALMOND_LEAVES_SET);
    }

    private void addKwamiFoods() {
        // Ladybug
        tag(MineraculousItemTags.LADYBUG_KWAMI_PREFERRED_FOODS)
                .add(Items.BREAD);

        tag(MineraculousItemTags.LADYBUG_KWAMI_TREATS)
                .add(MineraculousItems.MACARON)
                .add(Items.COOKIE)
                .add(Items.CAKE);

        // Cat
        tag(MineraculousItemTags.CAT_KWAMI_PREFERRED_FOODS)
                .addTag(MineraculousItemTags.CHEESES_FOODS)
                .addTag(MineraculousItemTags.CHEESE_BLOCKS_FOODS);

        tag(MineraculousItemTags.CAT_KWAMI_TREATS)
                .addTag(MineraculousItemTags.CAMEMBERT)
                .addTag(MineraculousItemTags.CAMEMBERT_BLOCKS);

        // Butterfly
        tag(MineraculousItemTags.BUTTERFLY_KWAMI_PREFERRED_FOODS)
                .addTag(ItemTags.SMALL_FLOWERS);

        tag(MineraculousItemTags.BUTTERFLY_KWAMI_TREATS)
                .add(MineraculousBlocks.HIBISCUS_BUSH.asItem());
    }

    private void addFoods() {
        tag(ConventionalItemTags.FOODS)
                .add(MineraculousItems.RAW_MACARON)
                .add(MineraculousItems.MACARON)
                .add(MineraculousItems.ALMOND)
                .add(MineraculousItems.ROASTED_ALMOND)
                .addTag(MineraculousItemTags.CHEESES_FOODS);

        tag(ItemTags.DYEABLE)
                .add(MineraculousItems.RAW_MACARON);

        tag(MineraculousItemTags.CHEESES_FOODS)
                .addTag(MineraculousItemTags.CHEESE)
                .addTag(MineraculousItemTags.CAMEMBERT);

        ItemLikeTagAppender cheese = tag(MineraculousItemTags.CHEESE);
        MineraculousItems.CHEESE.values().forEach(cheese::add);
        ItemLikeTagAppender camembert = tag(MineraculousItemTags.CAMEMBERT);
        MineraculousItems.CAMEMBERT.values().forEach(camembert::add);

        // Blocks
        tag(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS)
                .addTag(MineraculousItemTags.CHEESE_BLOCKS_FOODS);

        copy(MineraculousBlockTags.CHEESE_BLOCKS_FOODS, MineraculousItemTags.CHEESE_BLOCKS_FOODS);
        copy(MineraculousBlockTags.CHEESE_BLOCKS, MineraculousItemTags.CHEESE_BLOCKS);
        copy(MineraculousBlockTags.CAMEMBERT_BLOCKS, MineraculousItemTags.CAMEMBERT_BLOCKS);
    }

    private void addArmors() {
        armorSet(MineraculousArmors.MIRACULOUS);
        armorSet(MineraculousArmors.KAMIKOTIZATION);
    }

    private void addArmorTrims() {
        tag(ItemTags.TRIM_TEMPLATES)
                .add(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE)
                .add(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE)
                .add(MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE);
    }

    private void addCurios() {
        curios(MineraculousItems.MIRACULOUS.get(),
                MineraculousCuriosProvider.SLOT_BROOCH,
                MineraculousCuriosProvider.SLOT_RING,
                MineraculousCuriosProvider.SLOT_EARRINGS);

        curios(MineraculousCuriosProvider.SLOT_BACK,
                MineraculousItems.BUTTERFLY_CANE.get());

        curios(MineraculousCuriosProvider.SLOT_BELT,
                MineraculousItems.CAT_STAFF.get(),
                MineraculousItems.LADYBUG_YOYO.get());
    }

    private void addAbilities() {
        // Lucky Charm
        tag(MineraculousItemTags.LUCKY_CHARM_SHADER_IMMUNE)
                .add(MineraculousItems.GREAT_SWORD);

        tag(MineraculousItemTags.SHOOTING_PROJECTILES)
                .add(MineraculousItems.BUTTERFLY_CANE)
                .add(MineraculousItems.CAT_STAFF)
                .add(Items.TRIDENT)
                .addTag(ItemTags.ARROWS);

        tag(MineraculousItemTags.GENERIC_LUCKY_CHARMS)
                .add(Items.APPLE)
                .addTag(ConventionalItemTags.TOOLS)
                .add(Items.RABBIT_FOOT)
                .addTag(ConventionalItemTags.GOLDEN_FOODS);

        tag(MineraculousItemTags.WARDEN_DISTRACTORS)
                .add(Items.SNOWBALL)
                .add(Items.EGG)
                .add(Items.SNOW_GOLEM_SPAWN_EGG);

        // Cataclysm
        tag(MineraculousItemTags.CATACLYSM_IMMUNE)
                .add(MineraculousItems.CATACLYSM_DUST.get())
                .addOptionalTag(ConventionalItemTags.UNBREAKABLE_BLOCKS);

        tag(ItemTags.SWORDS)
                .add(MineraculousItems.GREAT_SWORD);
    }
}
