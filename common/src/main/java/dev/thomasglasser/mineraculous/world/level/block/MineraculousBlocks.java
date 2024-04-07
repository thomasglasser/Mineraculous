package dev.thomasglasser.mineraculous.world.level.block;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.level.block.BlockUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;

public class MineraculousBlocks
{
	public static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK, Mineraculous.MOD_ID);

	public static final RegistryObject<CataclysmBlock> CATACLYSM_BLOCK = registerWithItem("cataclysm_block", () -> new CataclysmBlock(Block.Properties.of().noCollission()), List.of(CreativeModeTabs.NATURAL_BLOCKS));

	private static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> block, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return BlockUtils.registerBlockAndItemAndWrap(BLOCKS, name, block, MineraculousItems::register, tabs);
	}

	public static void init () {}
}
