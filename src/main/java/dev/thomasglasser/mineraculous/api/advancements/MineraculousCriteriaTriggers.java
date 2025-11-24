package dev.thomasglasser.mineraculous.api.advancements;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.advancements.critereon.KamikotizedEntityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.PerformedKamikotizationActiveAbilityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.PerformedMiraculousActiveAbilityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.ReleasedPurifiedEntitiesTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformedKamikotizationTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformedMiraculousTrigger;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousCriteriaTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MineraculousConstants.MOD_ID);

    // Miraculous
    public static final DeferredHolder<CriterionTrigger<?>, TransformedMiraculousTrigger> TRANSFORMED_MIRACULOUS = CRITERION_TRIGGERS.register("transformed_miraculous", TransformedMiraculousTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PerformedMiraculousActiveAbilityTrigger> PERFORMED_MIRACULOUS_ACTIVE_ABILITY = CRITERION_TRIGGERS.register("performed_miraculous_active_ability", PerformedMiraculousActiveAbilityTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, KamikotizedEntityTrigger> KAMIKOTIZED_ENTITY = CRITERION_TRIGGERS.register("kamikotized_entity", KamikotizedEntityTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ReleasedPurifiedEntitiesTrigger> RELEASED_PURIFIED_ENTITIES = CRITERION_TRIGGERS.register("released_purified_entities", ReleasedPurifiedEntitiesTrigger::new);

    // Kamikotization
    public static final DeferredHolder<CriterionTrigger<?>, TransformedKamikotizationTrigger> TRANSFORMED_KAMIKOTIZATION = CRITERION_TRIGGERS.register("transformed_kamikotization", TransformedKamikotizationTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PerformedKamikotizationActiveAbilityTrigger> PERFORMED_KAMIKOTIZATION_ACTIVE_ABILITY = CRITERION_TRIGGERS.register("performed_kamikotization_active_ability", PerformedKamikotizationActiveAbilityTrigger::new);

    @ApiStatus.Internal
    public static void init() {}
}
