package dev.thomasglasser.miraculous.tags;

import dev.thomasglasser.miraculous.Miraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MiraculousItemTags
{
	public static final TagKey<Item> MIRACULOUS = create("miraculous");

	private static TagKey<Item> create(String name)
	{
		return TagKey.create(Registries.ITEM, Miraculous.modLoc(name));
	}
}
