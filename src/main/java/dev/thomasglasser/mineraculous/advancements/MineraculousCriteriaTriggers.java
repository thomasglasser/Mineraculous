package dev.thomasglasser.mineraculous.advancements;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizePlayerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.ReleasePurifiedKamikoTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.TransformKamikotizationTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.TransformMiraculousTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.UseKamikotizationPowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.UseMiraculousPowerTrigger;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

public class MineraculousCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Mineraculous.MOD_ID);

    // Miraculous
    public static final DeferredHolder<CriterionTrigger<?>, TransformMiraculousTrigger> TRANSFORMED_MIRACULOUS = CRITERION_TRIGGERS.register("transformed_miraculous", TransformMiraculousTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, UseMiraculousPowerTrigger> USED_MIRACULOUS_POWER = CRITERION_TRIGGERS.register("used_miraculous_power", UseMiraculousPowerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, KamikotizePlayerTrigger> KAMIKOTIZED_PLAYER = CRITERION_TRIGGERS.register("kamikotized_player", KamikotizePlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ReleasePurifiedKamikoTrigger> RELEASED_PURIFIED_KAMIKO = CRITERION_TRIGGERS.register("released_purified_kamiko", ReleasePurifiedKamikoTrigger::new);

    // Kamikotization
    public static final DeferredHolder<CriterionTrigger<?>, TransformKamikotizationTrigger> TRANSFORMED_KAMIKOTIZATION = CRITERION_TRIGGERS.register("transformed_kamikotization", TransformKamikotizationTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, UseKamikotizationPowerTrigger> USED_KAMIKOTIZATION_POWER = CRITERION_TRIGGERS.register("used_kamikotization_power", UseKamikotizationPowerTrigger::new);

    public static void init() {}
}
