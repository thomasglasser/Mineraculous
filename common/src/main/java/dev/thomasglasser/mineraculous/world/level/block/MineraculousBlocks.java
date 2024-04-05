package dev.thomasglasser.mineraculous.world.level.block;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class MineraculousBlocks
{
	public static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK, Mineraculous.MOD_ID);

	public static final RegistryObject<CataclysmBlock> CATACLYSM_BLOCK = BLOCKS.register("cataclysm_block", () -> new CataclysmBlock(Block.Properties.of().noCollission()));

	public static void init () {}
}
