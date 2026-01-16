package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousItemSubPredicates {
    private static final DeferredRegister<ItemSubPredicate.Type<?>> ITEM_SUB_PREDICATES = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<ItemLuckyCharmPredicate>> LUCKY_CHARM = register("lucky_charm", ItemLuckyCharmPredicate.CODEC);

    private static <T extends ItemSubPredicate> DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<T>> register(String name, Codec<T> codec) {
        return ITEM_SUB_PREDICATES.register(name, () -> new ItemSubPredicate.Type<>(codec));
    }

    @ApiStatus.Internal
    public static void init() {}
}
