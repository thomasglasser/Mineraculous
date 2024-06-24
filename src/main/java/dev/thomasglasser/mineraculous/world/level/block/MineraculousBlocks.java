package dev.thomasglasser.mineraculous.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.level.block.BlockUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.List;
import java.util.SortedMap;
import java.util.function.Supplier;

public class MineraculousBlocks
{
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Mineraculous.MOD_ID);

	public static final DeferredBlock<CataclysmBlock> CATACLYSM_BLOCK = registerWithItem("cataclysm_block", () -> new CataclysmBlock(Block.Properties.of().noCollission()), List.of(CreativeModeTabs.NATURAL_BLOCKS));

	// Cheese
	public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> CHEESE_BLOCKS = cheese("cheese", MineraculousFoods.CHEESE, MineraculousItems.CHEESE_WEDGES);
	public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> WAXED_CHEESE_BLOCKS = waxed("cheese");
	public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> CAMEMBERT_BLOCKS = cheese("camembert", MineraculousFoods.CAMEMBERT, MineraculousItems.CAMEMBERT_WEDGES);
	public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> WAXED_CAMEMBERT_BLOCKS = waxed("camembert");
	private static final Supplier<BiMap<DeferredBlock<CheeseBlock>, DeferredBlock<CheeseBlock>>> WAXABLES = Suppliers.memoize(
			() -> {
				ImmutableBiMap.Builder<DeferredBlock<CheeseBlock>, DeferredBlock<CheeseBlock>> builder = ImmutableBiMap.builder();
				MineraculousBlocks.CHEESE_BLOCKS.forEach((age, block) -> builder.put(block, MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(age)));
				MineraculousBlocks.CAMEMBERT_BLOCKS.forEach((age, block) -> builder.put(block, MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(age)));
				return builder.build();
			}
	);
	private static final Supplier<BiMap<DeferredBlock<CheeseBlock>, DeferredBlock<CheeseBlock>>> UNWAXABLES = Suppliers.memoize(
			() -> WAXABLES.get().inverse()
	);

	private static SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> cheese(String name, FoodProperties foodProperties, SortedMap<CheeseBlock.Age, DeferredItem<?>> wedgeMap) {
		SortedMap<CheeseBlock.Age,DeferredBlock<CheeseBlock>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			cheese.put(age, registerWithItem(age.getSerializedName() + "_" + name + "_block", () -> new CheeseBlock(age, false, cheese.get(age.getNext()), WAXABLES.get().get(cheese.get(age)), null, wedgeMap.get(age), foodProperties, BlockBehaviour.Properties.of().strength(0.5f).forceSolidOn().sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()), List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return cheese;
	}

	private static SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxed(String name) {
		SortedMap<CheeseBlock.Age,DeferredBlock<CheeseBlock>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			cheese.put(age, registerWithItem("waxed_" + age.getSerializedName() + "_" + name + "_block", () -> new CheeseBlock(age, true, cheese.get(age.getNext()), null, UNWAXABLES.get().get(cheese.get(age)), null, null, BlockBehaviour.Properties.of().strength(1f).forceSolidOn().sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()), List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return cheese;
	}

	private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Supplier<T> block, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return BlockUtils.registerBlockAndItemAndWrap(BLOCKS, name, block, MineraculousItems::register, tabs);
	}

	public static void init () {}
}
