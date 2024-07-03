package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedItemTagsProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.tags.ConventionalItemTags;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousItemTagsProvider extends ExtendedItemTagsProvider {
    public MineraculousItemTagsProvider(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, Mineraculous.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(MineraculousItemTags.TIKKI_FOODS)
                .add(Items.BREAD);

        // TODO: Macaroons
        tag(MineraculousItemTags.TIKKI_TREATS)
                .add(Items.COOKIE)
                .add(Items.CAKE);

        tag(MineraculousItemTags.PLAGG_FOODS)
                .addTag(MineraculousItemTags.CHEESES_FOODS);

        tag(MineraculousItemTags.PLAGG_TREATS)
                .addTag(MineraculousItemTags.CAMEMBERT);

        tag(MineraculousItemTags.CHEESES_FOODS)
                .addTag(MineraculousItemTags.CHEESE)
                .addTag(MineraculousItemTags.CAMEMBERT);

        tag(MineraculousItemTags.CHEESES_BLOCKS_FOODS)
                .addTag(MineraculousItemTags.CHEESE_BLOCKS)
                .addTag(MineraculousItemTags.CAMEMBERT_BLOCKS);

        tag(ConventionalItemTags.FOODS)
                .addTag(MineraculousItemTags.CHEESES_FOODS);

        tag(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS)
                .addTag(MineraculousItemTags.CHEESES_BLOCKS_FOODS);

        cheese(MineraculousItemTags.CHEESE, MineraculousItemTags.CHEESE_BLOCKS, MineraculousItems.CHEESE_WEDGES, MineraculousBlocks.CHEESE_BLOCKS, MineraculousBlocks.WAXED_CHEESE_BLOCKS);
        cheese(MineraculousItemTags.CAMEMBERT, MineraculousItemTags.CAMEMBERT_BLOCKS, MineraculousItems.CAMEMBERT_WEDGES, MineraculousBlocks.CAMEMBERT_BLOCKS, MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS);

        tag(MineraculousItemTags.CATACLYSM_IMMUNE)
                .add(MineraculousItems.CATACLYSM_DUST.get())
                .addOptionalTag(ConventionalItemTags.UNBREAKABLE_BLOCKS);

        MineraculousArmors.MIRACULOUS_SETS.forEach(this::armorSet);

        curios("ring", MineraculousItems.CAT_MIRACULOUS.get());
    }

    protected void curios(String neoSlot, Item... items) {
        IntrinsicTagAppender<Item> curios = tag(TagKey.create(Registries.ITEM, Mineraculous.Dependencies.CURIOS.modLoc(neoSlot)));

        for (Item item : items) {
            curios.add(item);
        }
    }

    protected void cheese(IntrinsicTagAppender<Item> tag, IntrinsicTagAppender<Item> blockTag, Map<CheeseBlock.Age, DeferredItem<?>> wedges, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> blocks, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxed) {
        wedges.values().stream().map(DeferredItem::get).forEach(tag::add);
        blocks.values().stream().map(DeferredBlock::asItem).forEach(item -> {
            tag.add(item);
            blockTag.add(item);
        });
        waxed.values().stream().map(DeferredBlock::asItem).forEach(tag::add);
    }

    protected void cheese(TagKey<Item> tag, TagKey<Item> blockTag, Map<CheeseBlock.Age, DeferredItem<?>> wedges, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> blocks, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxed) {
        cheese(tag(tag), tag(blockTag), wedges, blocks, waxed);
    }
}
