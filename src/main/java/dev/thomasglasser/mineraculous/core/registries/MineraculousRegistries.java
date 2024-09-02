package dev.thomasglasser.mineraculous.core.registries;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class MineraculousRegistries {
    public static final ResourceKey<Registry<Miraculous>> MIRACULOUS = createRegistryKey("miraculous");
    public static final ResourceKey<Registry<MapCodec<? extends Ability>>> ABILITY_SERIALIZER = createRegistryKey("ability_serializer");
    public static final ResourceKey<Registry<Ability>> ABILITY = createRegistryKey("ability");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String pName) {
        return ResourceKey.createRegistryKey(Mineraculous.modLoc(pName));
    }

    public static void init() {}
}
