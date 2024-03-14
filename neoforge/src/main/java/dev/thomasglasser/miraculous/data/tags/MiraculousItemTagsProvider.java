package dev.thomasglasser.miraculous.data.tags;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.tags.MiraculousItemTags;
import dev.thomasglasser.miraculous.world.item.MiraculousItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiraculousItemTagsProvider extends ItemTagsProvider
{
	public MiraculousItemTagsProvider(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(p_275343_, p_275729_, p_275322_, Miraculous.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider pProvider)
	{
		tag(MiraculousItemTags.MIRACULOUS)
				.add(MiraculousItems.CAT_MIRACULOUS.get());

		tag(MiraculousItemTags.DESTRUCTION_KWAMI_FOOD)
				.add(Items.HONEY_BOTTLE)
				.add(Items.MILK_BUCKET);
	}
}
