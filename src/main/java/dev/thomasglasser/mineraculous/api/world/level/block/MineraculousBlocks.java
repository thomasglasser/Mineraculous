package dev.thomasglasser.mineraculous.api.world.level.block;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.world.level.block.OvenBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.level.block.BlockUtils;
import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousBlocks {
    @ApiStatus.Internal
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MineraculousConstants.MOD_ID);

    /// Sand-like filler replacement block for {@link dev.thomasglasser.mineraculous.api.world.ability.Abilities#CATACLYSM}.
    public static final DeferredBlock<CrumblingBlock> CATACLYSM_BLOCK = registerWithItem("cataclysm_block", () -> new CrumblingBlock(Block.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.SNARE).sound(SoundType.SAND)));

    /// No function currently, only used in {@link dev.thomasglasser.mineraculous.impl.world.entity.ai.village.poi.MineraculousPoiTypes#FROMAGER}.
    public static final DeferredBlock<Block> CHEESE_POT = registerWithItem("cheese_pot", () -> new Block(BlockBehaviour.Properties.of().strength(0.5f).noOcclusion().sound(SoundType.METAL).mapColor(MapColor.GOLD)));
    public static final DeferredBlock<OvenBlock> OVEN = registerWithItem("oven", () -> new OvenBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOKER)));

    public static final DeferredBlock<SelfDroppingBerryBushBlock> HIBISCUS_BUSH = registerWithSeparatelyNamedItem("hibiscus_bush", "hibiscus", () -> new SelfDroppingBerryBushBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));

    // Cheese
    public static final EnumMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> CHEESE = cheeses("cheese", MineraculousFoods.CHEESE, MapColor.GOLD, () -> MineraculousItems.CHEESE);
    public static final EnumMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> CAMEMBERT = cheeses("camembert", MineraculousFoods.CAMEMBERT, MapColor.TERRACOTTA_WHITE, () -> MineraculousItems.CAMEMBERT);
    public static final EnumMap<AgeingCheese.Age, DeferredBlock<PieceBlock>> WAXED_CHEESE = waxedCheeses("cheese", MapColor.GOLD, () -> MineraculousItems.WAXED_CHEESE);
    public static final EnumMap<AgeingCheese.Age, DeferredBlock<PieceBlock>> WAXED_CAMEMBERT = waxedCheeses("camembert", MapColor.TERRACOTTA_WHITE, () -> MineraculousItems.WAXED_CAMEMBERT);

    private static EnumMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> cheeses(String name, FoodProperties foodProperties, MapColor color, Supplier<EnumMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>>> wedges) {
        BlockBehaviour.Properties blockProperties = BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.SPONGE).mapColor(color).randomTicks();
        Item.Properties itemProperties = new Item.Properties().stacksTo(4);
        EnumMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> cheeses = new EnumMap<>(AgeingCheese.Age.class);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            cheeses.put(age, registerWithItem(age.getSerializedName() + "_" + name + "_block", () -> new AgeingCheeseEdibleFullBlock(age, foodProperties, wedges.get().get(age), blockProperties), itemProperties));
        }
        return cheeses;
    }

    private static EnumMap<AgeingCheese.Age, DeferredBlock<PieceBlock>> waxedCheeses(String name, MapColor color, Supplier<EnumMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>>> wedges) {
        BlockBehaviour.Properties blockProperties = BlockBehaviour.Properties.of().strength(0.75f).sound(SoundType.SPONGE).mapColor(color);
        Item.Properties itemProperties = new Item.Properties().stacksTo(4);
        EnumMap<AgeingCheese.Age, DeferredBlock<PieceBlock>> cheeses = new EnumMap<>(AgeingCheese.Age.class);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            cheeses.put(age, registerWithItem("waxed_" + age.getSerializedName() + "_" + name + "_block", () -> new SmallFourPieceBlock(wedges.get().get(age), blockProperties), itemProperties));
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
