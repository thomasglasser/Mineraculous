package dev.thomasglasser.mineraculous.data.datamaps;

import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.world.level.storage.loot.MineraculousGiftLootKeys;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;

public class MineraculousDataMapProvider extends DataMapProvider {
    public MineraculousDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        builder(MineraculousDataMaps.LUCKY_CHARMS)
                .build();

        builder(NeoForgeDataMaps.RAID_HERO_GIFTS)
                .add(MineraculousVillagerProfessions.FROMAGER, new RaidHeroGift(MineraculousGiftLootKeys.FROMAGER_GIFT), false)
                .build();
    }
}
