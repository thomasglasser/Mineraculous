package dev.thomasglasser.mineraculous.api.world.level.block;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.impl.world.level.block.CataclysmBlock;
import dev.thomasglasser.mineraculous.impl.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.impl.world.level.block.HibiscusBushBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.level.block.BlockUtils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
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
import org.jetbrains.annotations.ApiStatus;

public class MineraculousBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Mineraculous.MOD_ID);

    public static final DeferredBlock<CataclysmBlock> CATACLYSM_BLOCK = registerWithItem("cataclysm_block", () -> new CataclysmBlock(Block.Properties.of().noCollission().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.SNARE).sound(SoundType.SAND)));

    public static final DeferredBlock<Block> CHEESE_POT = registerWithItem("cheese_pot", () -> new Block(BlockBehaviour.Properties.of().strength(0.5f).noOcclusion().sound(SoundType.METAL).mapColor(MapColor.GOLD)));

    public static final DeferredBlock<HibiscusBushBlock> HIBISCUS_BUSH = registerWithSeparatelyNamedItem("hibiscus_bush", "hibiscus", () -> new HibiscusBushBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));

    // Cheese
    public static final SortedMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> CHEESE = cheeses("cheese", MineraculousFoods.CHEESE, MapColor.GOLD, () -> MineraculousItems.CHEESE);
    public static final SortedMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> CAMEMBERT = cheeses("camembert", MineraculousFoods.CAMEMBERT, MapColor.TERRACOTTA_WHITE, () -> MineraculousItems.CAMEMBERT);

    public static final SortedMap<AgeingCheese.Age, DeferredBlock<CheeseBlock>> WAXED_CHEESE = waxedCheeses("cheese", MapColor.GOLD, () -> MineraculousItems.WAXED_CHEESE);
    public static final SortedMap<AgeingCheese.Age, DeferredBlock<CheeseBlock>> WAXED_CAMEMBERT = waxedCheeses("camembert", MapColor.TERRACOTTA_WHITE, () -> MineraculousItems.WAXED_CAMEMBERT);

    private static SortedMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> cheeses(String name, FoodProperties foodProperties, MapColor color, Supplier<SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>>> wedges) {
        BlockBehaviour.Properties blockProperties = BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.SPONGE).mapColor(color).randomTicks();
        Item.Properties itemProperties = new Item.Properties().stacksTo(4);
        SortedMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> cheeses = new Reference2ObjectLinkedOpenHashMap<>(AgeingCheese.Age.values().length);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            cheeses.put(age, registerWithItem(age.getSerializedName() + "_" + name + "_block", () -> new AgeingCheeseEdibleFullBlock(age, foodProperties, wedges.get().get(age), blockProperties), itemProperties));
        }
        return cheeses;
    }

    private static SortedMap<AgeingCheese.Age, DeferredBlock<CheeseBlock>> waxedCheeses(String name, MapColor color, Supplier<SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>>> wedges) {
        BlockBehaviour.Properties blockProperties = BlockBehaviour.Properties.of().strength(0.75f).sound(SoundType.SPONGE).mapColor(color);
        Item.Properties itemProperties = new Item.Properties().stacksTo(4);
        SortedMap<AgeingCheese.Age, DeferredBlock<CheeseBlock>> cheeses = new Reference2ObjectLinkedOpenHashMap<>(AgeingCheese.Age.values().length);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            cheeses.put(age, registerWithItem("waxed_" + age.getSerializedName() + "_" + name + "_block", () -> new CheeseBlock(wedges.get().get(age), blockProperties), itemProperties));
        }
        return cheeses;
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

    @ApiStatus.Internal
    public static void init() {}
}
