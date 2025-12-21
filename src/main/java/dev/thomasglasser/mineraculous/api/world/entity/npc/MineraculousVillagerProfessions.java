package dev.thomasglasser.mineraculous.api.world.entity.npc;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.world.entity.ai.village.poi.MineraculousPoiTypes;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class MineraculousVillagerProfessions {
    private static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MineraculousConstants.MOD_ID);

    /// Found in the Creamery, sells cheeses
    public static final DeferredHolder<VillagerProfession, VillagerProfession> FROMAGER = register("fromager", MineraculousPoiTypes.FROMAGER.getKey(), SoundEvents.VILLAGER_WORK_CLERIC);
    /// Found in the Bakery, sells macarons
    public static final DeferredHolder<VillagerProfession, VillagerProfession> BAKER = register("baker", MineraculousPoiTypes.BAKER.getKey(), SoundEvents.VILLAGER_WORK_BUTCHER);

    private static DeferredHolder<VillagerProfession, VillagerProfession> register(String name, ResourceKey<PoiType> jobSite, @Nullable SoundEvent workSound) {
        return register(name, p_219668_ -> p_219668_.is(jobSite), p_219640_ -> p_219640_.is(jobSite), workSound);
    }

    private static DeferredHolder<VillagerProfession, VillagerProfession> register(
            String name, Predicate<Holder<PoiType>> heldJobSite, Predicate<Holder<PoiType>> acquirableJobSites, @Nullable SoundEvent workSound) {
        return register(name, heldJobSite, acquirableJobSites, ImmutableSet.of(), ImmutableSet.of(), workSound);
    }

    private static DeferredHolder<VillagerProfession, VillagerProfession> register(
            String name,
            Predicate<Holder<PoiType>> heldJobSite,
            Predicate<Holder<PoiType>> acquirableJobSites,
            ImmutableSet<Item> requestedItems,
            ImmutableSet<Block> secondaryPoi,
            @Nullable SoundEvent workSound) {
        return VILLAGER_PROFESSIONS.register(name, () -> new VillagerProfession(name, heldJobSite, acquirableJobSites, requestedItems, secondaryPoi, workSound));
    }

    @ApiStatus.Internal
    public static void init() {}
}
