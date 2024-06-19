package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MineraculousBlockTags
{
	public static final TagKey<Block> CATACLYSM_IMMUNE = create("cataclysm_immune");

	private static TagKey<Block> create(String name)
	{
		return TagKey.create(Registries.BLOCK, Mineraculous.modLoc(name));
	}

}
