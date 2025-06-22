package dev.thomasglasser.mineraculous.api.advancements;

import dev.thomasglasser.mineraculous.api.advancements.critereon.KamikotizeEntityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.PerformKamikotizationActiveAbilityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.PerformMiraculousActiveAbilityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.ReleasePurifiedEntitiesTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformKamikotizationTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformMiraculousTrigger;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousCriteriaTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Mineraculous.MOD_ID);

    // Miraculous
    public static final DeferredHolder<CriterionTrigger<?>, TransformMiraculousTrigger> TRANSFORMED_MIRACULOUS = CRITERION_TRIGGERS.register("transformed_miraculous", TransformMiraculousTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PerformMiraculousActiveAbilityTrigger> PERFORMED_MIRACULOUS_ACTIVE_ABILITY = CRITERION_TRIGGERS.register("performed_miraculous_active_ability", PerformMiraculousActiveAbilityTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, KamikotizeEntityTrigger> KAMIKOTIZED_ENTITY = CRITERION_TRIGGERS.register("kamikotized_entity", KamikotizeEntityTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ReleasePurifiedEntitiesTrigger> RELEASED_PURIFIED_ENTITIES = CRITERION_TRIGGERS.register("released_purified_entities", ReleasePurifiedEntitiesTrigger::new);

    // Kamikotization
    public static final DeferredHolder<CriterionTrigger<?>, TransformKamikotizationTrigger> TRANSFORMED_KAMIKOTIZATION = CRITERION_TRIGGERS.register("transformed_kamikotization", TransformKamikotizationTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PerformKamikotizationActiveAbilityTrigger> PERFORMED_KAMIKOTIZATION_ACTIVE_ABILITY = CRITERION_TRIGGERS.register("performed_kamikotization_active_ability", PerformKamikotizationActiveAbilityTrigger::new);

    @ApiStatus.Internal
    public static void init() {}
}
