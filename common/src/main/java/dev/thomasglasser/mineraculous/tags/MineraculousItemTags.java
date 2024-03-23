package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MineraculousItemTags
{
	public static final TagKey<Item> DESTRUCTION_KWAMI_FOOD = create("destruction_kwami_food");

	private static TagKey<Item> create(String name)
	{
		return TagKey.create(Registries.ITEM, Mineraculous.modLoc(name));
	}
}
