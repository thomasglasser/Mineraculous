package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousEntitySubPredicates {
    private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends EntitySubPredicate>, MapCodec<KamikotizationPredicate>> KAMIKOTIZATION = ENTITY_SUB_PREDICATES.register("kamikotization", () -> KamikotizationPredicate.CODEC);

    @ApiStatus.Internal
    public static void init() {}
}
