package dev.thomasglasser.mineraculous.datamaps;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class MineraculousDataMaps {
    public static final DataMapType<EntityType<?>, LuckyCharms> LUCKY_CHARMS = DataMapType.builder(Mineraculous.modLoc("lucky_charms"), Registries.ENTITY_TYPE, LuckyCharms.CODEC).build();
}
