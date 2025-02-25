package dev.thomasglasser.mineraculous.datamaps;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class MineraculousDataMaps {
    // Lucky Charms
    public static final DataMapType<Kamikotization, LuckyCharms> KAMIKOTIZATION_LUCKY_CHARMS = DataMapType.builder(Mineraculous.modLoc("lucky_charms"), MineraculousRegistries.KAMIKOTIZATION, LuckyCharms.CODEC).build();
    public static final DataMapType<Miraculous, LuckyCharms> MIRACULOUS_LUCKY_CHARMS = DataMapType.builder(Mineraculous.modLoc("lucky_charms"), MineraculousRegistries.MIRACULOUS, LuckyCharms.CODEC).build();
    public static final DataMapType<EntityType<?>, LuckyCharms> ENTITY_LUCKY_CHARMS = DataMapType.builder(Mineraculous.modLoc("lucky_charms"), Registries.ENTITY_TYPE, LuckyCharms.CODEC).build();

    // Mob Effects
    public static final DataMapType<MobEffect, Integer> MIRACULOUS_EFFECTS = DataMapType.builder(Mineraculous.modLoc("miraculous_effects"), Registries.MOB_EFFECT, ExtraCodecs.NON_NEGATIVE_INT).build();
}
