package dev.thomasglasser.mineraculous.world.effect;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public class MineraculousMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Mineraculous.MOD_ID);

    public static final DeferredHolder<MobEffect, CataclysmMobEffect> CATACLYSMED = MOB_EFFECTS.register("cataclysmed", () -> new CataclysmMobEffect(0x60ff0b));

    public static void init() {}
}
