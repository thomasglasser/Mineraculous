package dev.thomasglasser.mineraculous.api.sounds;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Mineraculous.MOD_ID);

    // Abilities
    public static final DeferredHolder<SoundEvent, SoundEvent> CATACLYSM_ACTIVATE = register("cataclysm", "ability", "activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATACLYSM_USE = register("cataclysm", "ability", "use");

    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_ACTIVATE = register("kamikotization", "ability", "activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZED_COMMUNICATION_ACTIVATE = register("kamikotized_communication", "ability", "activate");

    public static final DeferredHolder<SoundEvent, SoundEvent> LUCKY_CHARM_ACTIVATE = register("lucky_charm", "ability", "activate");

    public static final DeferredHolder<SoundEvent, SoundEvent> MIRACULOUS_LADYBUG_ACTIVATE = register("miraculous_ladybug", "ability", "activate");

    // Items
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_SHIELD = register("generic", "item", "shield");
    public static final DeferredHolder<SoundEvent, SoundEvent> LADYBUG_YOYO_SHIELD = register("ladybug_yoyo", "item", "shield");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAT_STAFF_EXTEND = register("cat_staff", "item", "extend");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAT_STAFF_RETRACT = register("cat_staff", "item", "retract");

    // Miraculous
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_TRANSFORM = register("generic", "miraculous", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_DETRANSFORM = register("generic", "miraculous", "detransform");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_TIMER_WARNING = register("generic", "miraculous", "timer_warning");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_TIMER_END = register("generic", "miraculous", "timer_end");
    public static final DeferredHolder<SoundEvent, SoundEvent> LADYBUG_TRANSFORM = register("ladybug", "miraculous", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAT_TRANSFORM = register("cat", "miraculous", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTERFLY_TRANSFORM = register("butterfly", "miraculous", "transform");

    // Kamikotization
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_BEGIN = register("kamikotization", "begin");
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_TRANSFORM = register("kamikotization", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_DETRANSFORM = register("kamikotization", "detransform");

    // Entities
    public static final DeferredHolder<SoundEvent, SoundEvent> KWAMI_HURT = register("kwami", "entity", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KWAMI_SUMMON = register("kwami", "entity", "summon");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String subject, String type, String name) {
        return SOUND_EVENTS.register(subject + "_" + name, () -> SoundEvent.createVariableRangeEvent(Mineraculous.modLoc(type + "." + subject + "." + name)));
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String subject, String name) {
        return SOUND_EVENTS.register(subject + "_" + name, () -> SoundEvent.createVariableRangeEvent(Mineraculous.modLoc(subject + "." + name)));
    }

    @ApiStatus.Internal
    public static void init() {}
}
