package dev.thomasglasser.mineraculous.advancements;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousTransformTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

public class MineraculousCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, MiraculousTransformTrigger> TRANSFORMED_MIRACULOUS = CRITERION_TRIGGERS.register("transformed_miraculous", MiraculousTransformTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, MiraculousUsePowerTrigger> USED_MIRACULOUS_POWER = CRITERION_TRIGGERS.register("used_miraculous_power", MiraculousUsePowerTrigger::new);

    public static void init() {}
}
