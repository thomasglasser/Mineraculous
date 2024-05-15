package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MineraculousItemTags
{
	// Kwami Foods and Treats
	public static final TagKey<Item> TIKKI_FOODS = create("tikki_foods");
	public static final TagKey<Item> TIKKI_TREATS = create("tikki_treats");
	public static final TagKey<Item> PLAGG_FOODS = create("plagg_foods");
	public static final TagKey<Item> PLAGG_TREATS = create("plagg_treats");

	// Blocks
	public static final TagKey<Item> CATACLYSM_IMMUNE = create("cataclysm_immune");

	// Cheeses
	// TODO: Move to TommyLib
	public static final TagKey<Item> CHEESES_FOODS = TagKey.create(Registries.ITEM, new ResourceLocation("c:foods/cheeses"));
	public static final TagKey<Item> CHEESE = create("cheese");
	public static final TagKey<Item> CAMEMBERT = create("camembert");

	private static TagKey<Item> create(String name)
	{
		return TagKey.create(Registries.ITEM, Mineraculous.modLoc(name));
	}
}
