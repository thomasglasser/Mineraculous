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

	// Cheese
	public static final Map<CheeseBlock.Age, RegistryObject<CheeseBlock>> CHEESE_BLOCKS = cheese("cheese");
	public static final Map<CheeseBlock.Age, RegistryObject<CheeseBlock>> WAXED_CHEESE_BLOCKS = waxed("cheese");
	public static final Map<CheeseBlock.Age, RegistryObject<CheeseBlock>> CAMEMBERT_BLOCKS = cheese("camembert");
	public static final Map<CheeseBlock.Age, RegistryObject<CheeseBlock>> WAXED_CAMEMBERT_BLOCKS = waxed("camembert");


	private static Map<CheeseBlock.Age, RegistryObject<CheeseBlock>> cheese(String name) {
		Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> cheese = new HashMap<>(CheeseBlock.Age.values().length);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			cheese.put(age, registerWithItem(age.getSerializedName() + "_" + name + "_block", () -> new CheeseBlock(age, false, BlockBehaviour.Properties.of().strength(0.5f).forceSolidOn().sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()), List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return cheese;
	}

	private static Map<CheeseBlock.Age, RegistryObject<CheeseBlock>> waxed(String name) {
		Map<CheeseBlock.Age,RegistryObject<CheeseBlock>> cheese = new HashMap<>(CheeseBlock.Age.values().length);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			cheese.put(age, registerWithItem("waxed_" + age.getSerializedName() + "_" + name + "_block", () -> new CheeseBlock(age, true, BlockBehaviour.Properties.of().strength(1f).forceSolidOn().sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()), List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return cheese;
	}

	private static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> block, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return BlockUtils.registerBlockAndItemAndWrap(BLOCKS, name, block, MineraculousItems::register, tabs);
	}

	public static void init () {}
}
