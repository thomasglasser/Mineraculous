package dev.thomasglasser.mineraculous.api.world.effect;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.effect.CataclysmMobEffect;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousMobEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Mineraculous.MOD_ID);

    public static final DeferredHolder<MobEffect, CataclysmMobEffect> CATACLYSM = MOB_EFFECTS.register("cataclysm", () -> new CataclysmMobEffect(0x60ff0b));

    @ApiStatus.Internal
    public static void init() {}
}
