package dev.thomasglasser.mineraculous.impl.data.datamaps;

import dev.thomasglasser.mineraculous.api.datamaps.Ageable;
import dev.thomasglasser.mineraculous.api.datamaps.EffectAmplifier;
import dev.thomasglasser.mineraculous.api.datamaps.LuckyCharms;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.datamaps.ModifierSettings;
import dev.thomasglasser.mineraculous.api.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.MineraculousGiftLootKeys;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.MineraculousLuckyCharmLootKeys;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

public class MineraculousDataMapProvider extends DataMapProvider {
    public MineraculousDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        // Lucky Charms
        builder(MineraculousDataMaps.MIRACULOUS_LUCKY_CHARMS)
                .add(Miraculouses.LADYBUG, new LuckyCharms(MineraculousLuckyCharmLootKeys.LADYBUG_MIRACULOUS), false)
                .add(Miraculouses.CAT, new LuckyCharms(MineraculousLuckyCharmLootKeys.CAT_MIRACULOUS), false)
                .add(Miraculouses.BUTTERFLY, new LuckyCharms(MineraculousLuckyCharmLootKeys.BUTTERFLY_MIRACULOUS), false)
                .build();
        builder(MineraculousDataMaps.ENTITY_LUCKY_CHARMS)
                .add(EntityTypeTags.RAIDERS, new LuckyCharms(MineraculousLuckyCharmLootKeys.RAID), false)
                .add(EntityType.ENDER_DRAGON.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.ENDER_DRAGON), false)
                .add(EntityType.ELDER_GUARDIAN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.ELDER_GUARDIAN), false)
                .add(EntityType.WARDEN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.WARDEN), false)
                .add(EntityType.WITHER.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.WITHER), false)
                .build();

        // Miraculous Buffs
        builder(MineraculousDataMaps.MIRACULOUS_EFFECTS)
                .add(MobEffects.DAMAGE_RESISTANCE, new EffectAmplifier(0), false)
                .add(MobEffects.DAMAGE_BOOST, new EffectAmplifier(0), false)
                .add(MobEffects.MOVEMENT_SPEED, new EffectAmplifier(0), false)
                .add(MobEffects.DIG_SPEED, new EffectAmplifier(0), false)
                .add(MobEffects.JUMP, new EffectAmplifier(1), false)
                .add(MobEffects.REGENERATION, new EffectAmplifier(0), false)
                .add(MobEffects.HEALTH_BOOST, new EffectAmplifier(0), false)
                .add(MobEffects.SATURATION, new EffectAmplifier(0), false)
                .build();
        builder(MineraculousDataMaps.MIRACULOUS_ATTRIBUTE_MODIFIERS)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, new ModifierSettings(-0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), false)
                .build();

        // Cheese
        Builder<Waxable, Block> waxables = builder(NeoForgeDataMaps.WAXABLES);
        MineraculousBlocks.CHEESE.forEach((age, block) -> waxables.add(block, new Waxable(MineraculousBlocks.WAXED_CHEESE.get(age).get()), false));
        MineraculousBlocks.CAMEMBERT.forEach((age, block) -> waxables.add(block, new Waxable(MineraculousBlocks.WAXED_CAMEMBERT.get(age).get()), false));

        Builder<Ageable, Block> ageables = builder(MineraculousDataMaps.AGEABLES);
        for (int i = 0; i < AgeingCheese.Age.values().length - 1; i++) {
            AgeingCheese.Age age = AgeingCheese.Age.values()[i];
            AgeingCheese.Age next = age.getNext();
            ageables.add(MineraculousBlocks.CHEESE.get(age), new Ageable(MineraculousBlocks.CHEESE.get(next).get()), false);
            ageables.add(MineraculousBlocks.CAMEMBERT.get(age), new Ageable(MineraculousBlocks.CAMEMBERT.get(next).get()), false);
        }

        builder(NeoForgeDataMaps.COMPOSTABLES)
                .add(MineraculousBlocks.HIBISCUS_BUSH.asItem().builtInRegistryHolder().key().location(), new Compostable(0.65f), false);

        builder(NeoForgeDataMaps.RAID_HERO_GIFTS)
                .add(MineraculousVillagerProfessions.FROMAGER, new RaidHeroGift(MineraculousGiftLootKeys.FROMAGER_GIFT), false)
                .build();
    }
}
