package dev.thomasglasser.mineraculous.advancements;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizationTransformTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizationUsePowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizePlayerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousTransformTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.ReleasePurifiedKamikoTrigger;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

public class MineraculousCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, MiraculousTransformTrigger> TRANSFORMED_MIRACULOUS = CRITERION_TRIGGERS.register("transformed_miraculous", MiraculousTransformTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, MiraculousUsePowerTrigger> USED_MIRACULOUS_POWER = CRITERION_TRIGGERS.register("used_miraculous_power", MiraculousUsePowerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, KamikotizePlayerTrigger> KAMIKOTIZED_PLAYER = CRITERION_TRIGGERS.register("kamikotized_player", KamikotizePlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, KamikotizationTransformTrigger> TRANSFORMED_KAMIKOTIZATION = CRITERION_TRIGGERS.register("transformed_kamikotization", KamikotizationTransformTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, KamikotizationUsePowerTrigger> USED_KAMIKOTIZATION_POWER = CRITERION_TRIGGERS.register("used_kamikotization_power", KamikotizationUsePowerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ReleasePurifiedKamikoTrigger> RELEASED_PURIFIED_KAMIKO = CRITERION_TRIGGERS.register("released_purified_kamiko", ReleasePurifiedKamikoTrigger::new);

    public static void init() {}
}
