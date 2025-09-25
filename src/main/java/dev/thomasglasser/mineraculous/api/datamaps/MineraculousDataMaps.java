package dev.thomasglasser.mineraculous.api.datamaps;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class MineraculousDataMaps {
    // Lucky Charms
    public static final DataMapType<Kamikotization, LuckyCharms> KAMIKOTIZATION_LUCKY_CHARMS = DataMapType.builder(MineraculousConstants.modLoc("lucky_charms"), MineraculousRegistries.KAMIKOTIZATION, LuckyCharms.CODEC).build();
    public static final DataMapType<Miraculous, LuckyCharms> MIRACULOUS_LUCKY_CHARMS = DataMapType.builder(MineraculousConstants.modLoc("lucky_charms"), MineraculousRegistries.MIRACULOUS, LuckyCharms.CODEC).build();
    public static final DataMapType<EntityType<?>, LuckyCharms> ENTITY_LUCKY_CHARMS = DataMapType.builder(MineraculousConstants.modLoc("lucky_charms"), Registries.ENTITY_TYPE, LuckyCharms.CODEC).build();

    // Miraculous Buffs
    /// Multiplied by {@link MiraculousData#powerLevel()} when applied to a miraculous holder.
    public static final DataMapType<MobEffect, MiraculousEffect> MIRACULOUS_EFFECTS = DataMapType.builder(MineraculousConstants.modLoc("miraculous_effects"), Registries.MOB_EFFECT, MiraculousEffect.CODEC).build();
    /// Multiplied by {@link MiraculousData#powerLevel()} when applied to a miraculous holder.
    public static final DataMapType<Attribute, ModifierSettings> MIRACULOUS_ATTRIBUTE_MODIFIERS = DataMapType.builder(MineraculousConstants.modLoc("miraculous_attribute_modifiers"), Registries.ATTRIBUTE, ModifierSettings.CODEC).build();

    // Cheese
    public static final DataMapType<Block, Ageable> AGEABLES = DataMapType.builder(MineraculousConstants.modLoc("ageables"), Registries.BLOCK, Ageable.CODEC).build();
}
