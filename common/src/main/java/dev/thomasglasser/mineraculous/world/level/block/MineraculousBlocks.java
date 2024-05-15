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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MineraculousBlocks
{
	public static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK, Mineraculous.MOD_ID);

	public static final RegistryObject<CataclysmBlock> CATACLYSM_BLOCK = registerWithItem("cataclysm_block", () -> new CataclysmBlock(Block.Properties.of().noCollission()), List.of(CreativeModeTabs.NATURAL_BLOCKS));

	public static final Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> CHEESE_BLOCK = cheese();
	public static final Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> WAXED_CHEESE_BLOCK = waxed_cheese();
	public static final Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> CAMEMBERT_BLOCK = camembert();
	public static final Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> WAXED_CAMEMBERT_BLOCK = waxed_camembert();

	private static Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> cheese() {
		Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> cheese = new HashMap<>(5);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			cheese.put(age, registerWithItem(age.getSerializedName() + "_cheese_block", () -> new CheeseBlock(age, false, BlockBehaviour.Properties.of().strength(20, 2).sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()),List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return cheese;
	}

	private static Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> waxed_cheese() {
		Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> cheese = new HashMap<>(5);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			cheese.put(age, registerWithItem("waxed_" + age.getSerializedName() + "_cheese_block", () -> new CheeseBlock(age, true, BlockBehaviour.Properties.of().strength(20, 2).sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()),List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return cheese;
	}

	private static Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> camembert() {
		Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> camembert = new HashMap<>(5);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			camembert.put(age, registerWithItem(age.getSerializedName() + "_camembert_block", () -> new CheeseBlock(age, false, BlockBehaviour.Properties.of().strength(20, 2).sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()),List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return camembert;
	}

	private static Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> waxed_camembert() {
		Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> camembert = new HashMap<>(5);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			camembert.put(age, registerWithItem("waxed_" + age.getSerializedName() + "_camembert_block", () -> new CheeseBlock(age, true, BlockBehaviour.Properties.of().strength(20, 2).sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()),List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return camembert;
	}

	private static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> block, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return BlockUtils.registerBlockAndItemAndWrap(BLOCKS, name, block, MineraculousItems::register, tabs);
	}

	public static void init () {}
}
