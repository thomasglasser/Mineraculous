package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.level.storage.loot.MineraculousLuckyCharmLootKeys;
import dev.thomasglasser.mineraculous.world.level.storage.loot.predicates.HasItem;
import dev.thomasglasser.mineraculous.world.level.storage.loot.providers.number.PowerLevelMultiplierGenerator;
import dev.thomasglasser.tommylib.api.tags.ConventionalItemTags;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
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
                MineraculousLuckyCharmLootKeys.BLAZE,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.POTION)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)))
                                        .add(
                                                LootItem.lootTableItem(Items.SPLASH_POTION)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)))
                                        .add(
                                                LootItem.lootTableItem(Items.LINGERING_POTION)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.SPLASH_POTION)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.WATER)))
                                        .add(
                                                LootItem.lootTableItem(Items.LINGERING_POTION)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.WATER)))
                                        .add(
                                                LootItem.lootTableItem(Items.WATER_BUCKET)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                        .when(InvertedLootItemCondition.invert(LocationCheck.checkLocation(LocationPredicate.Builder.location().setDimension(Level.NETHER))))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.CREEPER,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.WOODEN_SWORD)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(4, 6)))
                                                                .fromOptions(HolderSet.direct(registries.holderOrThrow(Enchantments.KNOCKBACK)))))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.ENDERMAN,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.ENDER_PEARL)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16)))))
                                        .add(
                                                LootItem.lootTableItem(Items.WATER_BUCKET)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                        .add(
                                                LootItem.lootTableItem(Items.SPLASH_POTION)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.WATER)))
                                        .add(
                                                LootItem.lootTableItem(Items.LINGERING_POTION)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(SetPotionFunction.setPotion(Potions.WATER)))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.PIGLIN,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ItemTags.PIGLIN_LOVED)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 4))))
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(4, 6)))))
                                        .add(
                                                TagEntry.expandTag(ItemTags.PIGLIN_REPELLENTS)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 4))))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ConventionalItemTags.CROSSBOW_TOOLS)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(4, 6)))))
                                        .when(HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ItemTags.ARROWS))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.FIREWORK_ROCKET)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 4))))
                                                        .apply(SetComponentsFunction.setComponent(DataComponents.FIREWORKS, new Fireworks(20, List.of(
                                                                new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, IntList.of(0xf7121d), IntList.of(0x15000b), true, true))))))
                                        .add(
                                                TagEntry.expandTag(ItemTags.ARROWS)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 4)))))
                                        .when(HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ConventionalItemTags.CROSSBOW_TOOLS)))));

        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.PILLAGER,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ConventionalItemTags.CROSSBOW_TOOLS)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(4, 6)))))
                                        .when(HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ItemTags.ARROWS))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.FIREWORK_ROCKET)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 4))))
                                                        .apply(SetComponentsFunction.setComponent(DataComponents.FIREWORKS, new Fireworks(3, List.of(
                                                                new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, IntList.of(0xf7121d), IntList.of(0x15000b), true, true))))))
                                        .add(
                                                TagEntry.expandTag(ItemTags.ARROWS)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16))))
                                                        .apply(SetPotionFunction.setPotion(Potions.LUCK)))
                                        .when(HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ConventionalItemTags.CROSSBOW_TOOLS)))));
        biConsumer.accept(
                MineraculousLuckyCharmLootKeys.SKELETON,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ConventionalItemTags.SHIELD_TOOLS)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(0, 3))))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ConventionalItemTags.BOW_TOOLS)
                                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, PowerLevelMultiplierGenerator.apply(UniformGenerator.between(0, 3)))))
                                        .when(HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ItemTags.ARROWS))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                TagEntry.expandTag(ItemTags.ARROWS)
                                                        .apply(SetItemCountFunction.setCount(PowerLevelMultiplierGenerator.apply(UniformGenerator.between(1, 16))))
                                                        .apply(SetPotionFunction.setPotion(Potions.LUCK)))
                                        .when(
                                                AnyOfCondition.anyOf(
                                                        HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ConventionalItemTags.BOW_TOOLS)),
                                                        HasItem.hasItemsMatching(ItemPredicate.Builder.item().of(ConventionalItemTags.CROSSBOW_TOOLS))))));
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
    }
}
