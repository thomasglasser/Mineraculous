package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.level.storage.loot.MineraculousLuckyCharmLootKeys;
import dev.thomasglasser.mineraculous.world.level.storage.loot.predicates.HasItem;
import dev.thomasglasser.mineraculous.world.level.storage.loot.providers.number.PowerLevelMultiplierGenerator;
import dev.thomasglasser.tommylib.api.tags.ConventionalItemTags;
import java.util.function.BiConsumer;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public record MineraculousLuckyCharmLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        // Miraculous
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.CAT_MIRACULOUS,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ConventionalItemTags.SHIELD_TOOLS)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(0, 3)))))));

        // Entities
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.ENDER_DRAGON,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.ENDER_PEARL)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16)))))
                                        .add(
                                                LootItem.lootTableItem(Items.WIND_CHARGE)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16)))))
                                        .add(
                                                LootItem.lootTableItem(Items.WATER_BUCKET)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                        .add(
                                                TagEntry.expandTag(ConventionalItemTags.POTIONS)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.LONG_SLOW_FALLING)))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.ELDER_GUARDIAN,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Blocks.SPONGE)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16)))))
                                        .add(
                                                LootItem.lootTableItem(Blocks.CONDUIT)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .when(HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE).withCount(MinMaxBounds.Ints.atLeast(16)))))
                                        .add(
                                                LootItem.lootTableItem(Blocks.PRISMARINE)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(16, 32))))
                                                        .when(HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(Blocks.CONDUIT))))
                                        .add(
                                                LootItem.lootTableItem(Items.MILK_BUCKET)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                        .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().effects(MobEffectsPredicate.Builder.effects().and(MobEffects.DIG_SLOWDOWN))))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.WARDEN,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(
                                                TagEntry.expandTag(MineraculousItemTags.WARDEN_DISTRACTORS)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16))))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(
                                                LootItem.lootTableItem(Items.MILK_BUCKET)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                        .add(
                                                LootItem.lootTableItem(Items.SPECTRAL_ARROW)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16))))
                                                        .when(AnyOfCondition.anyOf(
                                                                HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ConventionalItemTags.BOW_TOOLS)),
                                                                HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ConventionalItemTags.CROSSBOW_TOOLS)))))
                                        .when(
                                                LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().effects(MobEffectsPredicate.Builder.effects().and(MobEffects.DARKNESS))))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.WITHER,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Blocks.OBSIDIAN)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16)))))
                                        .add(
                                                LootItem.lootTableItem(Items.SHEEP_SPAWN_EGG)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16)))))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.RAID,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ConventionalItemTags.MELEE_WEAPON_TOOLS)
                                                        .apply(new EnchantRandomlyFunction.Builder().withOneOf(
                                                                HolderSet.direct(
                                                                        registries.holderOrThrow(Enchantments.KNOCKBACK),
                                                                        registries.holderOrThrow(Enchantments.SHARPNESS),
                                                                        registries.holderOrThrow(Enchantments.FIRE_ASPECT)))))
                                        .add(
                                                LootItem.lootTableItem(Items.BELL)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 3)))))
                                        .add(
                                                LootItem.lootTableItem(Items.SHIELD)
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(0, 3)))))
                                        .add(
                                                LootItem.lootTableItem(Items.IRON_GOLEM_SPAWN_EGG)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 3)))))
                                        .add(
                                                LootItem.lootTableItem(Items.CROSSBOW)
                                                        .apply(new EnchantRandomlyFunction.Builder().withEnchantment(registries.holderOrThrow(Enchantments.FLAME)))
                                                        .apply(new EnchantRandomlyFunction.Builder().withEnchantment(registries.holderOrThrow(Enchantments.QUICK_CHARGE)))
                                                        .apply(new EnchantRandomlyFunction.Builder().withEnchantment(registries.holderOrThrow(Enchantments.INFINITY))))
                                        .add(
                                                LootItem.lootTableItem(Items.IRON_BLOCK)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(4, 16)))))
                                        .add(
                                                LootItem.lootTableItem(Items.VILLAGER_SPAWN_EGG)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16)))))

                        ));
    }
}
