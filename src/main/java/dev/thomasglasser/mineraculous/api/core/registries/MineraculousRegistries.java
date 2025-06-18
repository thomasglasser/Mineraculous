package dev.thomasglasser.mineraculous.api.core.registries;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class MineraculousRegistries {
    /**
     * Static registry holding {@link MapCodec}s for {@link Ability}s,
     * used in serializing the {@link MineraculousRegistries#ABILITY} registry.
     */
    public static final ResourceKey<Registry<MapCodec<? extends Ability>>> ABILITY_SERIALIZER = create("ability_serializer");
    /// Data-driven registry holding {@link Ability}s based on {@link MineraculousRegistries#ABILITY_SERIALIZER} entries.
    public static final ResourceKey<Registry<Ability>> ABILITY = create("ability");
    /// Data-driven registry holding {@link Miraculous}es containing {@link MineraculousRegistries#ABILITY} entries.
    public static final ResourceKey<Registry<Miraculous>> MIRACULOUS = create("miraculous");
    /// Data-driven registry holding {@link Kamikotization}s containing {@link MineraculousRegistries#ABILITY} entries.
    public static final ResourceKey<Registry<Kamikotization>> KAMIKOTIZATION = create("kamikotization");

    private static <T> ResourceKey<Registry<T>> create(String name) {
        return ResourceKey.createRegistryKey(Mineraculous.modLoc(name));
    }
}
