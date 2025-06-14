package dev.thomasglasser.mineraculous.world.damagesource;

import dev.thomasglasser.mineraculous.Mineraculous;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class MineraculousDamageTypes {
    public static final ResourceKey<DamageType> CATACLYSM = create("cataclysm");

    public static void bootstrap(BootstrapContext<DamageType> context) {
        register(context, CATACLYSM, msgId -> new DamageType(msgId, 1));
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Mineraculous.modLoc(name));
    }

    private static void register(BootstrapContext<DamageType> context, ResourceKey<DamageType> key, Function<String, DamageType> damageTypeFunction) {
        context.register(key, damageTypeFunction.apply(key.location().getPath()));
    }
}
