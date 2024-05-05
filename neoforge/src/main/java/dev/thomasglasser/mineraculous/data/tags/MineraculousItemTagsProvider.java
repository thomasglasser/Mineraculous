package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedItemTagsProvider;
import dev.thomasglasser.tommylib.api.tags.TommyLibItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MineraculousItemTagsProvider extends ExtendedItemTagsProvider
{
	public MineraculousItemTagsProvider(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(p_275343_, p_275729_, p_275322_, Mineraculous.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider pProvider)
	{
		tag(MineraculousItemTags.TIKKI_FOODS)
				.add(Items.BREAD); // The charm in my galette.

		// TODO: Macaroons
		tag(MineraculousItemTags.TIKKI_TREATS);

		// TODO: Cheese
		tag(MineraculousItemTags.PLAGG_FOODS);

		// TODO: Camembert
		tag(MineraculousItemTags.PLAGG_TREATS);

		tag(MineraculousItemTags.CATACLYSM_IMMUNE)
				.add(MineraculousItems.CATACLYSM_DUST.get())
				.addOptionalTag(TommyLibItemTags.UNBREAKABLE);

		MineraculousArmors.MIRACULOUS_SETS.forEach(this::armorSet);

		curios("ring", "hand/ring", MineraculousItems.CAT_MIRACULOUS.get());
	}

	protected void curios(String neoSlot, String fabricSlot, Item... items)
	{
		IntrinsicTagAppender<Item> curios = tag(TagKey.create(Registries.ITEM, Mineraculous.Dependencies.CURIOS.neoLoc(neoSlot)));
		IntrinsicTagAppender<Item> trinkets = tag(TagKey.create(Registries.ITEM, Mineraculous.Dependencies.CURIOS.fabricLoc(fabricSlot)));

		for (Item item : items)
		{
			curios.add(item);
			trinkets.add(item);
		}
	}
}
