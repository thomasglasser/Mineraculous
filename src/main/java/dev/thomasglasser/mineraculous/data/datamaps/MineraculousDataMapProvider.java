package dev.thomasglasser.mineraculous.data.datamaps;

import dev.thomasglasser.mineraculous.datamaps.LuckyCharms;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.world.level.storage.loot.MineraculousGiftLootKeys;
import dev.thomasglasser.mineraculous.world.level.storage.loot.MineraculousLuckyCharmLootKeys;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;

public class MineraculousDataMapProvider extends DataMapProvider {
    public MineraculousDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        builder(MineraculousDataMaps.KAMIKOTIZATION_LUCKY_CHARMS)
                .build();
        builder(MineraculousDataMaps.MIRACULOUS_LUCKY_CHARMS)
                .add(MineraculousMiraculous.CAT, new LuckyCharms(MineraculousLuckyCharmLootKeys.CAT_MIRACULOUS_LUCKY_CHARM), false)
                .build();
        builder(MineraculousDataMaps.ENTITY_LUCKY_CHARMS)
                .add(EntityType.BLAZE.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.BLAZE_LUCKY_CHARM), false)
                .add(EntityType.CREEPER.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.CREEPER_LUCKY_CHARM), false)
                .add(EntityType.ENDERMAN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.ENDERMAN_LUCKY_CHARM), false)
//                .add(EntityType.GUARDIAN.builtInRegistryHolder(), new LuckyCharms(MineraculousItemTags.GUARDIAN_LUCKY_CHARMS), false)
                .add(EntityType.PIGLIN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.PIGLIN_LUCKY_CHARM), false)
                .add(EntityType.PILLAGER.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.PILLAGER_LUCKY_CHARM), false)
                .add(EntityType.SKELETON.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.SKELETON_LUCKY_CHARM), false)
                .add(EntityType.WARDEN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.WARDEN_LUCKY_CHARM), false)
                .build();

        builder(NeoForgeDataMaps.RAID_HERO_GIFTS)
                .add(MineraculousVillagerProfessions.FROMAGER, new RaidHeroGift(MineraculousGiftLootKeys.FROMAGER_GIFT), false)
                .build();
    }
}
