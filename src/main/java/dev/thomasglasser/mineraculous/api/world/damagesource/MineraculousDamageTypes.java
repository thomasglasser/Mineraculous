package dev.thomasglasser.mineraculous.api.world.damagesource;

import dev.thomasglasser.mineraculous.Mineraculous;
import java.util.function.Function;

import dev.thomasglasser.mineraculous.api.world.ability.Abilities;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousDamageTypes {
    /// Dealt by {@link MineraculousMobEffects#CATACLYSM} and {@link Abilities#CATACLYSM}
    public static final ResourceKey<DamageType> CATACLYSM = create("cataclysm");

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Mineraculous.modLoc(name));
    }

    private static void register(BootstrapContext<DamageType> context, ResourceKey<DamageType> key, Function<String, DamageType> damageTypeFunction) {
        context.register(key, damageTypeFunction.apply(key.location().getPath()));
    }

    @ApiStatus.Internal
    public static void bootstrap(BootstrapContext<DamageType> context) {
        register(context, CATACLYSM, msgId -> new DamageType(msgId, 1));
    }
}
