package dev.thomasglasser.mineraculous.sounds;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public class MineraculousSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Mineraculous.MOD_ID);

    // Abilities
    public static final DeferredHolder<SoundEvent, SoundEvent> CATACLYSM_ACTIVATE = register("cataclysm", "ability", "activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATACLYSM_USE = register("cataclysm", "ability", "use");

    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_ACTIVATE = register("kamikotization", "ability", "activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_USE = register("kamikotization", "ability", "use");

    // Items
    public static final DeferredHolder<SoundEvent, SoundEvent> CAT_STAFF_EXTEND = register("cat_staff", "item", "extend");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAT_STAFF_RETRACT = register("cat_staff", "item", "retract");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAT_STAFF_SHIELD = register("cat_staff", "item", "shield");

    // Miraculous
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_TRANSFORM = register("generic", "miraculous", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_DETRANSFORM = register("generic", "miraculous", "detransform");
    public static final DeferredHolder<SoundEvent, SoundEvent> LADYBUG_TRANSFORM = register("ladybug", "miraculous", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAT_TRANSFORM = register("cat", "miraculous", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTERFLY_TRANSFORM = register("butterfly", "miraculous", "transform");

    // Kamikotization
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_TRANSFORM = register("kamikotization", "transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMIKOTIZATION_DETRANSFORM = register("kamikotization", "detransform");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String subject, String type, String name) {
        return SOUND_EVENTS.register(subject + "_" + name, () -> SoundEvent.createVariableRangeEvent(Mineraculous.modLoc(type + "." + subject + "." + name)));
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String subject, String name) {
        return SOUND_EVENTS.register(subject + "_" + name, () -> SoundEvent.createVariableRangeEvent(Mineraculous.modLoc(subject + "." + name)));
    }

    public static void init() {}
}
