package dev.thomasglasser.mineraculous.world.damagesource;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class MineraculousDamageTypes {
    public static final ResourceKey<DamageType> CATACLYSM = create("cataclysm");

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(CATACLYSM, new DamageType("cataclysm", 0.0F));
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Mineraculous.modLoc(name));
    }
}
