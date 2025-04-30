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
import java.util.SortedMap;
import java.util.function.Supplier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class MineraculousBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Mineraculous.MOD_ID);

    public static final DeferredBlock<CataclysmBlock> CATACLYSM_BLOCK = registerWithItem("cataclysm_block", () -> new CataclysmBlock(Block.Properties.of().noCollission().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.SNARE).sound(SoundType.SAND)));

    public static final DeferredBlock<Block> CHEESE_POT = registerWithItem("cheese_pot", () -> new Block(BlockBehaviour.Properties.of().strength(0.5f).noOcclusion().sound(SoundType.METAL).mapColor(MapColor.GOLD)));

    public static final DeferredBlock<HibiscusBushBlock> HIBISCUS_BUSH = registerWithSeparatelyNamedItem("hibiscus_bush", "hibiscus", () -> new HibiscusBushBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));

    // Cheese
    public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> CHEESE_BLOCKS = cheese("cheese", MineraculousFoods.CHEESE, MineraculousItems.CHEESE_WEDGES);
    public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> WAXED_CHEESE_BLOCKS = waxed("cheese", MineraculousItems.WAXED_CHEESE_WEDGES);
    public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> CAMEMBERT_BLOCKS = cheese("camembert", MineraculousFoods.CAMEMBERT, MineraculousItems.CAMEMBERT_WEDGES);
    public static final SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> WAXED_CAMEMBERT_BLOCKS = waxed("camembert", MineraculousItems.WAXED_CAMEMBERT_WEDGES);
    private static final Supplier<BiMap<DeferredBlock<CheeseBlock>, DeferredBlock<CheeseBlock>>> WAXABLES = Suppliers.memoize(
            () -> {
                ImmutableBiMap.Builder<DeferredBlock<CheeseBlock>, DeferredBlock<CheeseBlock>> builder = ImmutableBiMap.builder();
                MineraculousBlocks.CHEESE_BLOCKS.forEach((age, block) -> builder.put(block, MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(age)));
                MineraculousBlocks.CAMEMBERT_BLOCKS.forEach((age, block) -> builder.put(block, MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(age)));
                return builder.build();
            });
    private static final Supplier<BiMap<DeferredBlock<CheeseBlock>, DeferredBlock<CheeseBlock>>> UNWAXABLES = Suppliers.memoize(
            () -> WAXABLES.get().inverse());

    private static SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> cheese(String name, FoodProperties foodProperties, SortedMap<CheeseBlock.Age, DeferredItem<?>> wedgeMap) {
        SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
        for (CheeseBlock.Age age : CheeseBlock.Age.values())
            cheese.put(age, registerWithItem(age.getSerializedName() + "_" + name + "_block", () -> new CheeseBlock(age, false, cheese.get(age.getNext()), WAXABLES.get().get(cheese.get(age)), null, wedgeMap.get(age), foodProperties, BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()), new Item.Properties().stacksTo(4)));
        return cheese;
    }

    private static SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxed(String name, SortedMap<CheeseBlock.Age, DeferredItem<?>> wedgeMap) {
        SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
        for (CheeseBlock.Age age : CheeseBlock.Age.values())
            cheese.put(age, registerWithItem("waxed_" + age.getSerializedName() + "_" + name + "_block", () -> new CheeseBlock(age, true, cheese.get(age.getNext()), null, UNWAXABLES.get().get(cheese.get(age)), wedgeMap.get(age), null, BlockBehaviour.Properties.of().strength(1f).sound(SoundType.SPONGE).mapColor(MapColor.GOLD).randomTicks()), new Item.Properties().stacksTo(4)));
        return cheese;
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        return BlockUtils.register(BLOCKS, name, block);
    }

    private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Supplier<T> block) {
        return BlockUtils.registerBlockAndItemAndWrap(BLOCKS, name, block, MineraculousItems.ITEMS);
    }

    private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Supplier<T> block, Item.Properties properties) {
        return BlockUtils.registerBlockAndItemAndWrap(BLOCKS, name, block, MineraculousItems.ITEMS, properties);
    }

    private static <T extends Block> DeferredBlock<T> registerWithSeparatelyNamedItem(String blockName, String itemName, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = register(blockName, block);
        MineraculousItems.register(itemName, () -> new ItemNameBlockItem(deferredBlock.get(), new Item.Properties()));
        return deferredBlock;
    }

    public static void init() {}
}
