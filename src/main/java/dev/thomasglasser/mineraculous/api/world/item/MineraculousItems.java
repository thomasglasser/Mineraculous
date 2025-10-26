package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.block.PieceBlock;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.item.KwamiItem;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.impl.world.item.armortrim.MineraculousTrimPatterns;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousItems {
    @ApiStatus.Internal
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MineraculousConstants.MOD_ID);

    // Miraculous Tools
    /// Default properties for Miraculous tools
    public static final UnaryOperator<Item.Properties> MIRACULOUS_TOOL_PROPERTIES = properties -> properties.fireResistant().stacksTo(1).rarity(Rarity.EPIC);
    /// Default properties for Miraculous tools with the {@link MineraculousDataComponents#ACTIVE} component
    public static final UnaryOperator<Item.Properties> MIRACULOUS_TOOL_PROPERTIES_WITH_ACTIVE = properties -> MIRACULOUS_TOOL_PROPERTIES.apply(properties).component(MineraculousDataComponents.ACTIVE, Active.DEFAULT);
    public static final DeferredItem<LadybugYoyoItem> LADYBUG_YOYO = register("ladybug_yoyo", () -> new LadybugYoyoItem(MIRACULOUS_TOOL_PROPERTIES_WITH_ACTIVE.apply(new Item.Properties())));
    public static final DeferredItem<CatStaffItem> CAT_STAFF = register("cat_staff", () -> new CatStaffItem(MIRACULOUS_TOOL_PROPERTIES_WITH_ACTIVE.apply(new Item.Properties()).component(MineraculousDataComponents.ACTIVE_SETTINGS, CatStaffItem.ACTIVE_SETTINGS)));
    public static final DeferredItem<ButterflyCaneItem> BUTTERFLY_CANE = register("butterfly_cane", () -> new ButterflyCaneItem(MIRACULOUS_TOOL_PROPERTIES.apply(new Item.Properties())));

    // Miraculous
    public static final DeferredItem<MiraculousItem> MIRACULOUS = register("miraculous", () -> new MiraculousItem(new Item.Properties().component(MineraculousDataComponents.CHARGED, true)));
    public static final DeferredItem<KwamiItem> KWAMI = register("kwami", () -> new KwamiItem(new Item.Properties()));

    public static final DeferredItem<SwordItem> GREAT_SWORD = register("great_sword", () -> new SwordItem(MineraculousTiers.MIRACULOUS, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 6, -3))));
    /// Inventory filler used for ability reversion
    public static final DeferredItem<Item> CATACLYSM_DUST = register("cataclysm_dust", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    // Smithing Templates
    public static final DeferredItem<SmithingTemplateItem> LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.LADYBUG);
    public static final DeferredItem<SmithingTemplateItem> CAT_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.CAT);
    public static final DeferredItem<SmithingTemplateItem> BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.BUTTERFLY);

    // Cheese Wedges
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> CHEESE = wedges("cheese", MineraculousFoods.CHEESE, MineraculousBlocks.CHEESE);
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> WAXED_CHEESE = waxedWedges("cheese", MineraculousBlocks.WAXED_CHEESE);
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> CAMEMBERT = wedges("camembert", MineraculousFoods.CAMEMBERT, MineraculousBlocks.CAMEMBERT);
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> WAXED_CAMEMBERT = waxedWedges("camembert", MineraculousBlocks.WAXED_CAMEMBERT);

    public static final DeferredItem<Item> RAW_MACARON = register("raw_macaron", () -> new Item(new Item.Properties().stacksTo(16).food(MineraculousFoods.RAW_MACARON)));
    public static final DeferredItem<Item> MACARON = register("macaron", () -> new Item(new Item.Properties().stacksTo(16).food(MineraculousFoods.MACARON)));

    private static SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> wedges(String name, FoodProperties foodProperties, SortedMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> blocks) {
        SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> cheeses = new Reference2ObjectLinkedOpenHashMap<>(AgeingCheese.Age.values().length);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            DeferredBlock<AgeingCheeseEdibleFullBlock> block = blocks.get(age);
            cheeses.put(age, register(age.getSerializedName() + "_" + name + "_wedge", () -> new ItemNameBlockItem(block.get(), new Item.Properties().stacksTo(16).food(foodProperties).component(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(block.get().getMissingPiecesProperty(), block.get().getMaxMissingPieces()))) {
                @Override
                public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {}
            }));
        }
        return cheeses;
    }

    private static SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> waxedWedges(String name, SortedMap<AgeingCheese.Age, DeferredBlock<PieceBlock>> blocks) {
        SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> cheeses = new Reference2ObjectLinkedOpenHashMap<>(AgeingCheese.Age.values().length);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            DeferredBlock<PieceBlock> block = blocks.get(age);
            cheeses.put(age, register("waxed_" + age.getSerializedName() + "_" + name + "_wedge", () -> new ItemNameBlockItem(block.get(), new Item.Properties().stacksTo(16).component(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(block.get().getMissingPiecesProperty(), block.get().getMaxMissingPieces()))) {
                @Override
                public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {}
            }));
        }
        return cheeses;
    }

    @ApiStatus.Internal
    public static <T extends Item> DeferredItem<T> register(String name, Supplier<T> item) {
        return ItemUtils.register(ITEMS, name, item);
    }

    private static <T extends Mob> DeferredItem<SpawnEggItem> registerSpawnEgg(DeferredHolder<EntityType<?>, EntityType<T>> entityType, int primaryColor, int secondaryColor) {
        return register(entityType.getKey().location().getPath() + "_spawn_egg", () -> new DeferredSpawnEggItem(entityType, primaryColor, secondaryColor, new Item.Properties()));
    }

    private static DeferredItem<SmithingTemplateItem> registerSmithingTemplate(ResourceKey<TrimPattern> pattern) {
        return ItemUtils.registerSmithingTemplate(ITEMS, pattern);
    }

    @ApiStatus.Internal
    public static void init() {}
}
