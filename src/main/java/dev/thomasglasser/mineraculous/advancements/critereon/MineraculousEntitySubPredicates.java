package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.Registries;

public class MineraculousEntitySubPredicates {
    public static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends EntitySubPredicate>, MapCodec<KamikotizationPredicate>> KAMIKOTIZATION = ENTITY_SUB_PREDICATES.register("kamikotization", () -> KamikotizationPredicate.CODEC);

    public static void init() {}
}
