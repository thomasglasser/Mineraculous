package dev.thomasglasser.mineraculous.world.entity.npc;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.ai.village.poi.MineraculousPoiTypes;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class MineraculousVillagerProfessions {
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Mineraculous.MOD_ID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> FROMAGER = register("fromager", MineraculousPoiTypes.FROMAGER.getKey(), null);

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

    public static void init() {}
}
