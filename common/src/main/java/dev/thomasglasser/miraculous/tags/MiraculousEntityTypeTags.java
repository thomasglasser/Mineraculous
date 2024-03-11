package dev.thomasglasser.miraculous.tags;

import dev.thomasglasser.miraculous.Miraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class MiraculousEntityTypeTags
{
	public static final TagKey<EntityType<?>> KWAMIS = create("kwamis");

	private static TagKey<EntityType<?>> create(String name)
	{
		return TagKey.create(Registries.ENTITY_TYPE, Miraculous.modLoc(name));
	}
}
