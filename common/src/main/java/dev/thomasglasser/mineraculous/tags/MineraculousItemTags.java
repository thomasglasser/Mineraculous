package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MineraculousItemTags
{
	public static final TagKey<Item> PLAGG_FOODS = create("plagg_foods");
	public static final TagKey<Item> PLAGG_TREATS = create("plagg_treats");
	public static final TagKey<Item> CATACLYSM_IMMUNE = create("cataclysm_immune");

	private static TagKey<Item> create(String name)
	{
		return TagKey.create(Registries.ITEM, Mineraculous.modLoc(name));
	}
}
