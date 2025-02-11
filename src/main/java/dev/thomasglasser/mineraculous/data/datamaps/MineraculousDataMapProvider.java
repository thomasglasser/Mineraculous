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
import net.minecraft.tags.EntityTypeTags;
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
                .add(MineraculousMiraculous.CAT, new LuckyCharms(MineraculousLuckyCharmLootKeys.CAT_MIRACULOUS), false)
                .build();
        builder(MineraculousDataMaps.ENTITY_LUCKY_CHARMS)
                .add(EntityTypeTags.SKELETONS, new LuckyCharms(MineraculousLuckyCharmLootKeys.SKELETON), false)
                .add(EntityType.BLAZE.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.BLAZE), false)
                .add(EntityType.CREEPER.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.CREEPER), false)
                .add(EntityType.ENDERMAN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.ENDERMAN), false)
//                .add(EntityType.GUARDIAN.builtInRegistryHolder(), new LuckyCharms(MineraculousItemTags.GUARDIAN_LUCKY_CHARMS), false)
                .add(EntityType.PIGLIN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.PIGLIN), false)
                .add(EntityType.PILLAGER.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.PILLAGER), false)
                .add(EntityType.WARDEN.builtInRegistryHolder(), new LuckyCharms(MineraculousLuckyCharmLootKeys.WARDEN), false)
                .build();

        builder(NeoForgeDataMaps.RAID_HERO_GIFTS)
                .add(MineraculousVillagerProfessions.FROMAGER, new RaidHeroGift(MineraculousGiftLootKeys.FROMAGER_GIFT), false)
                .build();
    }
}
